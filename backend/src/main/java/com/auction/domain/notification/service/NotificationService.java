package com.auction.domain.notification.service;

import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.entity.NotificationType;
import com.auction.domain.notification.repository.NotificationRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
import com.auction.infra.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스
 *
 * 사용자에게 알림을 전송하는 단일 창구 역할을 한다.
 * 모든 메서드는 @Async로 비동기 실행되어 메인 비즈니스 로직을 차단하지 않는다.
 *
 * 처리 순서:
 *   1. notifications 테이블에 알림 내역 저장 (isSent=false)
 *   2. 텔레그램 메시지 전송
 *   3. 전송 성공 시 isSent=true 로 갱신
 *
 * 텔레그램 전송 실패 시:
 *   - 예외를 전파하지 않고 경고 로그만 남긴다 (서비스 안정성 우선)
 *   - isSent=false 상태로 남아 향후 재처리 스케줄러 대상이 된다
 *
 * 주의: 메서드 인자로 엔티티를 전달하면 다른 스레드에서 detached 상태가 되므로,
 *       userId, title 등 원시 값만 인자로 받아 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;
    private final TelegramService        telegramService;

    // ── 알림 전송 메서드 (유형별) ─────────────────────────────────────────────────

    /**
     * 입찰 완료 알림
     * 📈 New bid registered on [item]! Current price: {price}원
     */
    @Async("asyncExecutor")
    @Transactional
    public void sendBidNotification(Long userId, String auctionTitle, Long bidPrice) {
        String message = String.format(
                "📈 New bid registered on <b>%s</b>! Current price: <b>%,d원</b>",
                auctionTitle, bidPrice);

        saveAndSend(userId, NotificationType.BID, message);
    }

    /**
     * 상위 입찰로 밀려남 알림
     * ⚠️ You've been outbid on [item]! Current: {price}원
     */
    @Async("asyncExecutor")
    @Transactional
    public void sendOutbidNotification(Long userId, String auctionTitle, Long currentPrice) {
        String message = String.format(
                "⚠️ You've been outbid on <b>%s</b>! Current: <b>%,d원</b>",
                auctionTitle, currentPrice);

        saveAndSend(userId, NotificationType.OUTBID, message);
    }

    /**
     * 경매 낙찰 알림
     * 🎉 Congratulations! You won [item] for {price}원
     */
    @Async("asyncExecutor")
    @Transactional
    public void sendWinNotification(Long userId, String auctionTitle, Long finalPrice) {
        String message = String.format(
                "🎉 Congratulations! You won <b>%s</b> for <b>%,d원</b>",
                auctionTitle, finalPrice);

        saveAndSend(userId, NotificationType.WIN, message);
    }

    /**
     * 경매 미낙찰 알림
     * 😢 You didn't win [item]. Final price: {price}원
     */
    @Async("asyncExecutor")
    @Transactional
    public void sendLoseNotification(Long userId, String auctionTitle, Long finalPrice) {
        String message = String.format(
                "😢 You didn't win <b>%s</b>. Final price: <b>%,d원</b>",
                auctionTitle, finalPrice);

        saveAndSend(userId, NotificationType.LOSE, message);
    }

    /**
     * 경매 1시간 전 마감 임박 알림
     * ⏰ [item] ends in 1 hour! Current: {price}원
     */
    @Async("asyncExecutor")
    @Transactional
    public void sendExpiryWarningNotification(Long userId, String auctionTitle, Long currentPrice) {
        String message = String.format(
                "⏰ <b>%s</b> ends in 1 hour! Current: <b>%,d원</b>",
                auctionTitle, currentPrice);

        saveAndSend(userId, NotificationType.EXPIRY_WARNING, message);
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /**
     * 알림을 DB에 저장하고 텔레그램으로 전송한다.
     *
     * getUserReferenceById로 프록시를 가져와 조인 컬럼만 저장하고,
     * telegramChatId는 실제 조회가 필요하므로 findById로 로드한다.
     */
    private void saveAndSend(Long userId, NotificationType type, String message) {
        // User 프록시 참조 (INSERT 시 FK만 필요)
        User userRef = userRepository.getReferenceById(userId);

        Notification notification = Notification.builder()
                .user(userRef)
                .type(type)
                .message(message)
                .build();
        notificationRepository.save(notification);

        // telegramChatId 조회를 위해 실제 User 로드
        userRepository.findById(userId).ifPresent(user -> {
            try {
                telegramService.sendMessage(user.getTelegramChatId(), message);
                notification.markAsSent();
            } catch (Exception e) {
                // 텔레그램 전송 실패: 로그만 남기고 isSent=false 유지 (재처리 가능)
                log.warn("텔레그램 알림 전송 실패 (userId={}, type={}): {}", userId, type, e.getMessage());
            }
        });
    }
}
