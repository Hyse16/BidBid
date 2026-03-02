package com.auction.global.exception;

import com.auction.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 컨트롤러 전역 예외 처리기
 *
 * @RestControllerAdvice를 통해 모든 컨트롤러에서 발생하는 예외를 한 곳에서 처리한다.
 * 예외 종류에 따라 적절한 HTTP 상태 코드와 에러 메시지를 ApiResponse 형태로 반환한다.
 *
 * 처리 대상:
 *   - CustomException            : 비즈니스 로직 예외 (ErrorCode 기반)
 *   - MethodArgumentNotValidException : @Valid 유효성 검사 실패
 *   - AccessDeniedException      : 권한 없음 (403)
 *   - AuthenticationException    : 인증 실패 (401)
 *   - Exception                  : 위에 해당하지 않는 모든 예외 (500)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 예외 처리: ErrorCode에 정의된 HTTP 상태와 메시지로 응답 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.warn("CustomException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }

    /** @Valid 유효성 검사 실패 처리: 실패한 모든 필드의 메시지를 하나로 합쳐 반환 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    /** 접근 권한 없음 (403 Forbidden) */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("AccessDeniedException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorCode.ACCESS_DENIED.getMessage()));
    }

    /** 인증 실패 (401 Unauthorized) - JWT 검증 실패 등 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("AuthenticationException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorCode.TOKEN_INVALID.getMessage()));
    }

    /** 예상치 못한 모든 예외 처리 (500 Internal Server Error) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
