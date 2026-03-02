package com.auction.domain.watchlist.dto;

import com.auction.domain.watchlist.entity.Watchlist;

import java.time.LocalDateTime;

/**
 * 관심 목록 응답 DTO
 *
 * 관심 등록한 경매 상품의 주요 정보를 담아 반환한다.
 * 썸네일 URL은 이미지 목록 중 isThumbnail=true인 항목에서 가져온다.
 */
public record WatchlistResponse(
        Long          watchlistId,   // 관심 목록 항목 ID
        Long          auctionId,     // 경매 ID
        String        title,         // 경매 상품 제목
        Long          currentPrice,  // 현재가
        Long          buyNowPrice,   // 즉시 구매가 (null 가능)
        String        status,        // 경매 상태
        LocalDateTime endAt,         // 종료 일시
        String        categoryName,  // 카테고리 이름
        String        thumbnailUrl,  // 썸네일 이미지 URL (null 가능)
        LocalDateTime addedAt        // 관심 목록 등록 일시
) {
    /**
     * Watchlist 엔티티 → WatchlistResponse 변환 팩토리 메서드
     *
     * auctionItem.images에서 썸네일을 찾아 URL을 추출한다.
     * images 컬렉션이 초기화되지 않은 경우 null을 반환하므로,
     * 트랜잭션 내에서 또는 fetch join 후 호출해야 한다.
     */
    public static WatchlistResponse from(Watchlist watchlist) {
        var auction = watchlist.getAuctionItem();

        // 썸네일 이미지 URL 추출
        String thumbnailUrl = auction.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .map(img -> img.getImageUrl())
                .findFirst()
                .orElse(null);

        return new WatchlistResponse(
                watchlist.getId(),
                auction.getId(),
                auction.getTitle(),
                auction.getCurrentPrice(),
                auction.getBuyNowPrice(),
                auction.getStatus().name(),
                auction.getEndAt(),
                auction.getCategory().getName(),
                thumbnailUrl,
                watchlist.getCreatedAt()
        );
    }
}
