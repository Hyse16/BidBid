package com.auction.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전체에서 사용하는 에러 코드 정의
 *
 * 각 에러는 HTTP 상태 코드와 메시지를 함께 가진다.
 * GlobalExceptionHandler에서 CustomException을 잡아 해당 상태 코드로 응답한다.
 *
 * 도메인별로 그룹화하여 관리:
 *   Auth      - 인증/인가 관련 오류
 *   User      - 사용자 관련 오류
 *   Auction   - 경매 관련 오류
 *   Bid       - 입찰 관련 오류
 *   Category  - 카테고리 관련 오류
 *   Watchlist - 관심 목록 관련 오류
 *   File/S3   - 파일 업로드 관련 오류
 *   Common    - 공통 오류
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 인증 / 인가 ─────────────────────────────────────────────────────────────
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),    // 이메일 또는 비밀번호 불일치
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),                  // 토큰 만료
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid token"),                      // 유효하지 않은 토큰
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Token not found"),                  // 토큰 없음
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),                         // 접근 권한 없음

    // ── 사용자 ───────────────────────────────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),                       // 사용자 없음
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already exists"),            // 이메일 중복
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Nickname already exists"),      // 닉네임 중복

    // ── 경매 ─────────────────────────────────────────────────────────────────────
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Auction not found"),                 // 경매 없음
    AUCTION_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "Auction is not active"),          // 진행 중이 아닌 경매
    AUCTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "Auction has already ended"),   // 이미 종료된 경매
    AUCTION_OWNER_CANNOT_BID(HttpStatus.BAD_REQUEST, "Auction owner cannot bid on their own item"), // 본인 경매에 입찰 불가
    AUCTION_NOT_OWNER(HttpStatus.FORBIDDEN, "Only the auction owner can perform this action"),      // 소유자만 수정 가능
    AUCTION_CANNOT_MODIFY(HttpStatus.BAD_REQUEST, "Cannot modify auction with existing bids"),      // 입찰이 있으면 수정 불가

    // ── 입찰 ─────────────────────────────────────────────────────────────────────
    BID_PRICE_TOO_LOW(HttpStatus.BAD_REQUEST, "Bid price must be higher than current price"),       // 현재가보다 낮은 입찰
    BID_UNIT_INVALID(HttpStatus.BAD_REQUEST, "Bid price does not meet minimum bid unit requirement"), // 최소 입찰 단위 미충족
    BID_LOCK_FAILED(HttpStatus.CONFLICT, "Failed to acquire bid lock, please try again"),           // Redis 분산 락 획득 실패

    // ── 카테고리 ─────────────────────────────────────────────────────────────────
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),              // 카테고리 없음

    // ── 관심 목록 ────────────────────────────────────────────────────────────────
    WATCHLIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "Item is already in your watchlist"),  // 이미 관심 목록에 추가됨
    WATCHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "Watchlist entry not found"),             // 관심 목록 항목 없음

    // ── 알림 ─────────────────────────────────────────────────────────────────────
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification not found"),             // 알림 없음

    // ── 파일 / S3 ─────────────────────────────────────────────────────────────────
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed"),  // S3 업로드 실패
    FILE_TYPE_INVALID(HttpStatus.BAD_REQUEST, "Invalid file type"),              // 허용되지 않은 파일 타입
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "File size exceeds the limit"),   // 파일 크기 초과 (5MB 제한)

    // ── 공통 ─────────────────────────────────────────────────────────────────────
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),                  // 잘못된 요청
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"); // 서버 내부 오류

    private final HttpStatus httpStatus;
    private final String message;
}
