package com.auction.domain.notification.repository;

import com.auction.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 알림 레포지토리
 *
 * 미전송 알림 조회는 향후 재처리 스케줄러에서 활용할 수 있다.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 특정 사용자의 미전송 알림 목록 조회 (재처리 스케줄러 확장용) */
    List<Notification> findByUserIdAndIsSentFalse(Long userId);
}
