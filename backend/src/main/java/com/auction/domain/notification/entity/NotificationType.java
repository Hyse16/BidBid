package com.auction.domain.notification.entity;

/**
 * 알림 유형 열거형
 *
 * 각 유형별 텔레그램 메시지 형식:
 *   BID            → "📈 New bid registered on [item]! Current price: 75,000원"
 *   OUTBID         → "⚠️ You've been outbid on [item]! Current: 80,000원"
 *   WIN            → "🎉 Congratulations! You won [item] for 95,000원"
 *   LOSE           → "😢 You didn't win [item]. Final price: 95,000원"
 *   EXPIRY_WARNING → "⏰ [item] ends in 1 hour! Current: 80,000원"
 */
public enum NotificationType {
    BID,            // 입찰 완료 (입찰자에게)
    OUTBID,         // 상위 입찰로 밀려남 (기존 최고 입찰자에게)
    WIN,            // 경매 낙찰 (낙찰자에게)
    LOSE,           // 경매 미낙찰 (입찰했지만 낙찰 못한 사용자에게)
    EXPIRY_WARNING  // 1시간 전 마감 임박 알림 (입찰자에게)
}
