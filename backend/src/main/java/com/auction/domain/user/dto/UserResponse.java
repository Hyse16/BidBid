package com.auction.domain.user.dto;

import com.auction.domain.user.entity.User;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 *
 * 비밀번호 등 민감한 정보는 제외하고 클라이언트에 반환한다.
 * User 엔티티를 직접 노출하지 않고 이 DTO로 변환하여 응답한다.
 */
public record UserResponse(
        Long          id,
        String        email,
        String        nickname,
        String        telegramChatId, // 텔레그램 알림 등록 여부 확인용
        String        role,           // "USER" 또는 "ADMIN"
        LocalDateTime createdAt
) {
    /**
     * User 엔티티 → UserResponse 변환 팩토리 메서드
     * 서비스 레이어에서 사용한다.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getTelegramChatId(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
