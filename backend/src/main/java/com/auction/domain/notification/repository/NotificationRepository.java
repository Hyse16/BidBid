package com.auction.domain.notification.repository;

import com.auction.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 레포지토리
 *
 * 알림함 조회 / 읽음 처리 / 미전송 재처리에 필요한 쿼리를 정의한다.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 특정 사용자의 전체 알림을 최신순으로 조회한다 (알림함 목록) */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 특정 사용자의 읽지 않은 알림 수를 반환한다 (알림 배지 카운트) */
    long countByUserIdAndIsReadFalse(Long userId);

    /** 알림 ID와 사용자 ID로 단건 조회한다 (소유자 검증 겸용) */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /** 특정 사용자의 미전송 알림 목록 조회 (재처리 스케줄러 확장용) */
    List<Notification> findByUserIdAndIsSentFalse(Long userId);
}
