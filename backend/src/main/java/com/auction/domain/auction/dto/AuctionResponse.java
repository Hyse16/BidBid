package com.auction.domain.auction.dto;

import com.auction.domain.auction.entity.AuctionItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 경매 상품 단건 상세 응답 DTO
 *
 * 이미지 URL 목록을 포함하여 상세 페이지에 필요한 모든 정보를 담는다.
 * AuctionItem 엔티티의 연관 객체(category, user, images)를 모두 접근하므로
 * 트랜잭션 내에서 생성해야 LazyInitializationException이 발생하지 않는다.
 */
public record AuctionResponse(
        Long            id,              // 경매 ID
        String          title,           // 상품 제목
        String          description,     // 상품 설명
        Long            startPrice,      // 시작가
        Long            currentPrice,    // 현재가
        Long            buyNowPrice,     // 즉시 구매가 (null 가능)
        Long            minBidUnit,      // 최소 입찰 단위
        String          status,          // 경매 상태 문자열
        LocalDateTime   startAt,         // 시작 일시
        LocalDateTime   endAt,           // 종료 일시
        String          categoryName,    // 카테고리 이름
        String          sellerNickname,  // 판매자 닉네임
        List<ImageInfo> images           // 이미지 목록
) {
    /** AuctionItem 엔티티로부터 상세 응답 DTO를 생성한다 */
    public static AuctionResponse from(AuctionItem item) {
        List<ImageInfo> imageInfos = item.getImages().stream()
                .map(img -> new ImageInfo(img.getImageUrl(), img.getIsThumbnail()))
                .toList();

        return new AuctionResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStartPrice(),
                item.getCurrentPrice(),
                item.getBuyNowPrice(),
                item.getMinBidUnit(),
                item.getStatus().name(),
                item.getStartAt(),
                item.getEndAt(),
                item.getCategory().getName(),
                item.getUser().getNickname(),
                imageInfos
        );
    }

    /** 이미지 정보 내부 레코드 */
    public record ImageInfo(
            String  url,         // S3 이미지 URL
            Boolean isThumbnail  // 썸네일 여부
    ) {}
}
