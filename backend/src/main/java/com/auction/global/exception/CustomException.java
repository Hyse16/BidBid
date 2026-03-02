package com.auction.global.exception;

import lombok.Getter;

/**
 * 애플리케이션 전용 런타임 예외
 *
 * 비즈니스 로직에서 발생하는 모든 예외는 이 클래스를 사용한다.
 * ErrorCode를 반드시 함께 전달하여 HTTP 상태 코드와 메시지를 일관되게 관리한다.
 *
 * 사용 예시:
 *   throw new CustomException(ErrorCode.USER_NOT_FOUND);
 *   throw new CustomException(ErrorCode.AUCTION_NOT_FOUND, "경매 ID: " + id);
 */
@Getter
public class CustomException extends RuntimeException {

    /** 에러 종류 및 HTTP 상태 코드 정보 */
    private final ErrorCode errorCode;

    /** 기본 메시지를 ErrorCode에서 가져오는 생성자 */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 추가 상세 메시지를 직접 지정하는 생성자 */
    public CustomException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}
