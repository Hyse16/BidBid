package com.auction.domain.notification.dto;

import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 *
 * 사용자 알림함에 표시되는 개별 알림 정보를 담는다.
 * isSent는 내부 전송 상태이므로 클라이언트에 노출하지 않는다.
 */
public record NotificationResponse(
        Long             id,
        NotificationType type,
        String           message,
        Boolean          isRead,
        LocalDateTime    createdAt
) {
    /** Notification 엔티티를 응답 DTO로 변환한다 */
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
