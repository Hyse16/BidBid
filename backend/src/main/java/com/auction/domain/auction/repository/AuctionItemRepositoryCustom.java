package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.AuctionListResponse;
import com.auction.domain.auction.dto.AuctionSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * QueryDSL 커스텀 레포지토리 인터페이스
 *
 * 동적 조건 검색이 필요한 경매 목록 조회 메서드를 정의한다.
 * 구현체는 AuctionItemRepositoryImpl에 위치한다.
 *
 * Spring Data JPA 명명 규칙:
 *   AuctionItemRepository + "Impl" 로 자동 감지된다.
 */
public interface AuctionItemRepositoryCustom {

    /**
     * 동적 조건으로 경매 목록을 페이징 조회한다.
     *
     * @param condition 검색 조건 (categoryId, status, keyword)
     * @param pageable  페이징 및 정렬 정보
     * @return 목록용 경매 응답 페이지
     */
    Page<AuctionListResponse> searchAuctions(AuctionSearchCondition condition, Pageable pageable);

    /**
     * 특정 사용자가 등록한 경매 목록을 최신순으로 조회한다 (썸네일 포함).
     *
     * 마이페이지 "내 경매 목록"에서 사용한다.
     *
     * @param userId 조회 대상 사용자 ID
     * @return 목록용 경매 응답 리스트
     */
    List<AuctionListResponse> findByUserIdOrderByCreatedAtDesc(Long userId);
}
