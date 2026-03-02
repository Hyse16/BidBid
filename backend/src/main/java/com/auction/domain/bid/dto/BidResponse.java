package com.auction.domain.bid.dto;

import com.auction.domain.bid.entity.Bid;

import java.time.LocalDateTime;

/**
 * 입찰 응답 DTO
 *
 * 단건 입찰 정보를 반환한다.
 * REST API 입찰 완료 응답 및 입찰 히스토리 목록에 사용된다.
 */
public record BidResponse(
        Long          id,             // 입찰 ID
        Long          auctionItemId,  // 경매 ID
        String        bidderNickname, // 입찰자 닉네임
        Long          bidPrice,       // 입찰가
        String        status,         // 입찰 상태 (WINNING / OUTBID / FAILED)
        LocalDateTime createdAt       // 입찰 일시
) {
    /** Bid 엔티티로부터 응답 DTO를 생성한다 */
    public static BidResponse from(Bid bid) {
        return new BidResponse(
                bid.getId(),
                bid.getAuctionItem().getId(),
                bid.getUser().getNickname(),
                bid.getBidPrice(),
                bid.getStatus().name(),
                bid.getCreatedAt()
        );
    }
}
