package com.auction.domain.bid.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 입찰 요청 DTO
 *
 * REST POST /api/auctions/{id}/bids 또는
 * WebSocket STOMP /app/auction/{id}/bid 로 전달된다.
 *
 * @param bidPrice 입찰가 (최소 1원, 실제 유효성은 서비스에서 재검증)
 */
public record BidRequest(
        @NotNull(message = "입찰가는 필수입니다")
        @Min(value = 1, message = "입찰가는 1원 이상이어야 합니다")
        Long bidPrice
) {}
