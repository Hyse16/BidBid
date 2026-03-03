package com.auction.domain.auction.dto;

import com.auction.domain.auction.entity.AuctionResult;

import java.time.LocalDateTime;

/**
 * 경매 낙찰 결과 응답 DTO
 *
 * 경매 종료 후 낙찰자와 최종 낙찰가를 클라이언트에 전달한다.
 * 유찰(입찰자 없이 종료)인 경우 AuctionResult가 존재하지 않으므로
 * API는 빈 데이터(null)를 반환한다.
 */
public record AuctionResultResponse(
        Long          auctionItemId,
        Long          finalPrice,
        String        winnerNickname,
        LocalDateTime settledAt           // 낙찰 확정 시각 (AuctionResult.createdAt)
) {
    /** AuctionResult 엔티티를 응답 DTO로 변환한다 */
    public static AuctionResultResponse from(AuctionResult result) {
        return new AuctionResultResponse(
                result.getAuctionItem().getId(),
                result.getFinalPrice(),
                result.getWinner().getNickname(),
                result.getCreatedAt()
        );
    }
}
