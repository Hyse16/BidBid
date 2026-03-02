package com.auction.domain.bid.dto;

import java.time.LocalDateTime;

/**
 * WebSocket 입찰 브로드캐스트 메시지
 *
 * 입찰 성공 시 /topic/auction/{id} 채널을 구독 중인
 * 모든 클라이언트에게 전송되는 실시간 업데이트 메시지.
 *
 * 프론트엔드는 이 메시지를 수신하여 현재가와 입찰자 정보를 즉시 갱신한다.
 *
 * 구독 방법 (SockJS + STOMP.js):
 *   stompClient.subscribe('/topic/auction/1', (msg) => {
 *     const data = JSON.parse(msg.body); // BidBroadcastMessage
 *     updateCurrentPrice(data.currentPrice);
 *   });
 *
 * @param auctionItemId  경매 ID
 * @param currentPrice   갱신된 현재가
 * @param bidderNickname 입찰자 닉네임
 * @param timestamp      입찰 발생 서버 시각
 */
public record BidBroadcastMessage(
        Long          auctionItemId,   // 경매 ID
        Long          currentPrice,    // 갱신된 현재가
        String        bidderNickname,  // 입찰자 닉네임
        LocalDateTime timestamp        // 입찰 발생 시각
) {}
