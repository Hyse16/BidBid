package com.auction.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 프로필 수정 요청 DTO
 *
 * 수정 가능한 필드: 닉네임, 텔레그램 Chat ID
 * 이메일과 비밀번호 변경은 별도 엔드포인트에서 처리한다.
 */
public record UserUpdateRequest(

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 50, message = "닉네임은 2~50자 사이여야 합니다")
        String nickname,

        String telegramChatId // 텔레그램 알림 수신용 Chat ID (선택, null 허용)
) {}
