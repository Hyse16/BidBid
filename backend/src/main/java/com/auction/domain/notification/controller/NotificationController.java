package com.auction.domain.notification.controller;

import com.auction.domain.notification.dto.NotificationResponse;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.user.entity.User;
import com.auction.global.response.ApiResponse;
import com.auction.global.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림함 컨트롤러
 *
 * 로그인한 사용자가 자신의 알림 내역을 조회하고 관리하는 API를 제공한다.
 * 모든 엔드포인트는 JWT 인증이 필요하다.
 *
 * 제공 기능:
 *   GET    /api/notifications              - 내 알림 목록 (최신순)
 *   GET    /api/notifications/unread-count - 읽지 않은 알림 수
 *   PATCH  /api/notifications/{id}/read   - 단일 알림 읽음 처리
 *   PATCH  /api/notifications/read-all    - 전체 알림 읽음 처리
 *   DELETE /api/notifications/{id}        - 알림 삭제
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService     notificationService;
    private final CustomUserDetailsService userDetailsService;

    // ── 알림 목록 조회 ────────────────────────────────────────────────────────────

    /**
     * 내 알림 목록 조회 (최신순)
     *
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        List<NotificationResponse> notifications = notificationService.getNotifications(user.getId());

        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    /**
     * 읽지 않은 알림 수 조회
     *
     * GET /api/notifications/unread-count
     * 응답: {"count": 5}
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user  = resolveUser(userDetails);
        long count = notificationService.getUnreadCount(user.getId());

        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    // ── 읽음 처리 ─────────────────────────────────────────────────────────────────

    /**
     * 단일 알림 읽음 처리
     *
     * PATCH /api/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        notificationService.markAsRead(id, user.getId());

        return ResponseEntity.ok(ApiResponse.ok("알림을 읽음 처리했습니다"));
    }

    /**
     * 전체 알림 일괄 읽음 처리
     *
     * PATCH /api/notifications/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        notificationService.markAllAsRead(user.getId());

        return ResponseEntity.ok(ApiResponse.ok("모든 알림을 읽음 처리했습니다"));
    }

    // ── 알림 삭제 ─────────────────────────────────────────────────────────────────

    /**
     * 단일 알림 삭제
     *
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        notificationService.deleteNotification(id, user.getId());

        return ResponseEntity.ok(ApiResponse.ok("알림이 삭제되었습니다"));
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /** UserDetails에서 도메인 User 엔티티로 변환한다 */
    private User resolveUser(UserDetails userDetails) {
        return userDetailsService.loadUserEntityByEmail(userDetails.getUsername());
    }
}
