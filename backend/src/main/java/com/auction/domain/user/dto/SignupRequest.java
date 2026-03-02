package com.auction.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 *
 * @Valid 어노테이션과 함께 사용하여 컨트롤러에서 입력값을 자동 검증한다.
 * 검증 실패 시 GlobalExceptionHandler가 400 Bad Request로 응답한다.
 */
public record SignupRequest(

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,       // 이메일 형식 필수

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,    // 최소 8자 이상

        @NotBlank(message = "Nickname is required")
        @Size(min = 2, max = 20, message = "Nickname must be between 2 and 20 characters")
        String nickname     // 2~20자 이내
) {}
