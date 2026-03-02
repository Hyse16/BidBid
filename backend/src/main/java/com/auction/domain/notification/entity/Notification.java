package com.auction.domain.notification.entity;

import com.auction.domain.user.entity.User;
import com.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 엔티티 (notifications 테이블)
 *
 * 사용자에게 발송된 모든 알림 내역을 DB에 기록한다.
 * 텔레그램 전송 성공 여부는 isSent 필드로 추적한다.
 *
 * 활용 목적:
 *   - 알림 이력 보관 (사용자 알림함 기능 확장 가능)
 *   - 텔레그램 미전송 알림 재처리 기반 마련 (isSent=false 조회)
 *
 * NotificationService에서 @Async 로 비동기 저장되므로
 * 메인 비즈니스 트랜잭션과 별개의 트랜잭션에서 처리된다.
 */
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 알림 수신자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // 알림 유형

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message; // 알림 메시지 (HTML 형식)

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSent = false; // 텔레그램 전송 성공 여부

    /** 텔레그램 전송 성공 시 호출하여 전송 상태를 갱신한다 */
    public void markAsSent() {
        this.isSent = true;
    }
}
