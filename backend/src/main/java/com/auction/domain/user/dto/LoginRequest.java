package com.auction.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 *
 * 이메일과 비밀번호만 받아 인증을 처리한다.
 * 검증 실패 시 GlobalExceptionHandler가 400 Bad Request로 응답한다.
 */
public record LoginRequest(

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,    // 가입 시 사용한 이메일

        @NotBlank(message = "Password is required")
        String password  // 평문 비밀번호 (서비스 레이어에서 BCrypt 검증)
) {}
