package com.auction.domain.auction.service;

import com.auction.domain.auction.dto.*;
import com.auction.domain.auction.entity.AuctionImage;
import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.category.entity.Category;
import com.auction.domain.category.repository.CategoryRepository;
import com.auction.domain.user.entity.User;
import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import com.auction.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 경매 상품 서비스 (CRUD + S3 이미지 처리)
 *
 * 주요 책임:
 *   - 경매 상품 등록 / 단건 조회 / 목록 조회 / 수정 / 삭제
 *   - S3 다중 이미지 업로드 및 삭제 (첫 번째 이미지 = 썸네일)
 *   - Redis 캐시 관리 (@Cacheable / @CacheEvict)
 *
 * 캐시 전략:
 *   "auction"  (단건) : TTL 5분  → 단건 조회 빈도 높음
 *   "auctions" (목록) : TTL 30초 → 등록/수정/삭제 시 즉시 evict
 *
 * 입찰 존재 여부 판단:
 *   Step 3에서는 currentPrice > startPrice 비교로 판단한다.
 *   Step 4(입찰 도메인)에서 BidRepository.existsByAuctionItemId()로 교체 예정.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final AuctionItemRepository auctionItemRepository;
    private final CategoryRepository    categoryRepository;
    private final S3Service             s3Service;

    private static final String S3_DIRECTORY = "auction-images"; // S3 저장 경로

    // ── 경매 등록 ─────────────────────────────────────────────────────────────────

    /**
     * 경매 상품을 등록하고 이미지를 S3에 업로드한다.
     *
     * 이미지 처리 순서:
     *   1. 첫 번째 이미지 → isThumbnail=true
     *   2. 나머지 이미지 → isThumbnail=false
     *
     * @param user    인증된 판매자
     * @param request 경매 등록 요청 DTO
     * @param images  첨부 이미지 목록 (null 허용 - 이미지 없이 등록 가능)
     * @return 등록된 경매의 상세 응답
     */
    @Transactional
    @CacheEvict(value = "auctions", allEntries = true)
    public AuctionResponse createAuction(User user, AuctionCreateRequest request, List<MultipartFile> images) {
        // 카테고리 유효성 확인
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 경매 상품 엔티티 생성 (초기 현재가 = 시작가)
        AuctionItem item = AuctionItem.builder()
                .user(user)
                .category(category)
                .title(request.title())
                .description(request.description())
                .startPrice(request.startPrice())
                .currentPrice(request.startPrice())
                .buyNowPrice(request.buyNowPrice())
                .minBidUnit(request.minBidUnit())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .build();

        // S3 이미지 업로드 후 엔티티에 연결
        if (images != null && !images.isEmpty()) {
            uploadImages(item, images);
        }

        AuctionItem saved = auctionItemRepository.save(item);

        return AuctionResponse.from(saved);
    }

    // ── 경매 단건 조회 ────────────────────────────────────────────────────────────

    /**
     * 경매 상품 단건을 이미지 포함하여 조회한다.
     *
     * "auction::{id}" 키로 5분간 캐싱된다.
     * fetch join으로 이미지를 한 번에 로드하여 N+1 문제를 방지한다.
     */
    @Cacheable(value = "auction", key = "#id")
    public AuctionResponse getAuction(Long id) {
        AuctionItem item = findAuctionByIdWithImages(id);

        return AuctionResponse.from(item);
    }

    // ── 경매 목록 조회 ────────────────────────────────────────────────────────────

    /**
     * 검색 조건과 페이징으로 경매 목록을 조회한다.
     *
     * QueryDSL로 카테고리 / 상태 / 키워드 동적 필터를 적용한다.
     * "auctions" 캐시에 30초간 저장된다.
     */
    @Cacheable(value = "auctions", key = "#condition.toString() + #pageable.toString()")
    public Page<AuctionListResponse> getAuctions(AuctionSearchCondition condition, Pageable pageable) {
        return auctionItemRepository.searchAuctions(condition, pageable);
    }

    // ── 경매 수정 ─────────────────────────────────────────────────────────────────

    /**
     * 경매 상품을 수정한다.
     *
     * 검증 조건:
     *   1. 소유자 본인만 수정 가능
     *   2. 입찰 기록이 있으면 수정 불가 (currentPrice > startPrice)
     *
     * 이미지가 전달된 경우: 기존 S3 이미지를 모두 삭제하고 신규 이미지로 교체한다.
     * 이미지가 없으면: 기존 이미지를 유지한다.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "auction",  key = "#id"),
            @CacheEvict(value = "auctions", allEntries = true)
    })
    public AuctionResponse updateAuction(Long id, User user, AuctionUpdateRequest request, List<MultipartFile> images) {
        AuctionItem item = findAuctionByIdWithImages(id);

        // 소유자 검증
        validateOwner(item, user);

        // 입찰 존재 시 수정 불가 (Step 4에서 BidRepository로 교체 예정)
        if (hasBids(item)) {
            throw new CustomException(ErrorCode.AUCTION_CANNOT_MODIFY);
        }

        // 카테고리 유효성 확인
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 기본 정보 업데이트
        item.update(
                request.title(),
                request.description(),
                category,
                request.buyNowPrice(),
                request.minBidUnit(),
                request.startAt(),
                request.endAt()
        );

        // 이미지 전달 시: 기존 S3 삭제 후 신규 이미지로 교체
        if (images != null && !images.isEmpty()) {
            deleteExistingImages(item);
            item.getImages().clear();
            uploadImages(item, images);
        }

        return AuctionResponse.from(item);
    }

    // ── 경매 삭제 ─────────────────────────────────────────────────────────────────

    /**
     * 경매 상품을 삭제한다.
     *
     * S3 이미지를 먼저 제거한 뒤 DB에서 삭제한다.
     * 소유자 본인만 삭제 가능하다.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "auction",  key = "#id"),
            @CacheEvict(value = "auctions", allEntries = true)
    })
    public void deleteAuction(Long id, User user) {
        AuctionItem item = findAuctionByIdWithImages(id);

        // 소유자 검증
        validateOwner(item, user);

        // S3 이미지 삭제 후 DB에서 제거
        deleteExistingImages(item);
        auctionItemRepository.delete(item);
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /** ID로 경매 상품을 이미지 포함하여 조회한다 (없으면 예외) */
    private AuctionItem findAuctionByIdWithImages(Long id) {
        return auctionItemRepository.findByIdWithImages(id)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    /** 요청한 사용자가 경매 소유자인지 검증한다 */
    private void validateOwner(AuctionItem item, User user) {
        if (!item.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.AUCTION_NOT_OWNER);
        }
    }

    /**
     * 입찰 기록이 있는지 확인한다.
     *
     * currentPrice가 startPrice보다 높으면 입찰이 있다고 판단한다.
     * Step 4에서 BidRepository.existsByAuctionItemId(item.getId())로 교체 예정.
     */
    private boolean hasBids(AuctionItem item) {
        return item.getCurrentPrice() > item.getStartPrice();
    }

    /** 이미지를 S3에 업로드하고 AuctionItem에 연결한다 (첫 번째 이미지 = 썸네일) */
    private void uploadImages(AuctionItem item, List<MultipartFile> images) {
        for (int i = 0; i < images.size(); i++) {
            String  url         = s3Service.upload(images.get(i), S3_DIRECTORY);
            boolean isThumbnail = (i == 0); // 첫 번째 이미지를 썸네일로 지정

            item.getImages().add(AuctionImage.of(item, url, isThumbnail));
        }
    }

    /** 경매 상품에 연결된 S3 이미지를 모두 삭제한다 */
    private void deleteExistingImages(AuctionItem item) {
        item.getImages().forEach(img -> {
            try {
                s3Service.delete(img.getImageUrl());
            } catch (Exception e) {
                // S3 삭제 실패 시 로그만 남기고 DB 삭제는 계속 진행
                log.warn("S3 이미지 삭제 실패 (계속 진행): {}", img.getImageUrl());
            }
        });
    }
}
