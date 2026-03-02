package com.auction.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모든 API 응답을 감싸는 공통 래퍼 클래스
 *
 * 응답 포맷을 일관되게 유지하기 위해 사용한다.
 * null 값 필드는 JSON 직렬화 시 제외된다 (@JsonInclude).
 *
 * 사용 예시:
 *   성공 (데이터 반환)       → ApiResponse.ok(data)
 *   성공 (메시지만)          → ApiResponse.ok("처리 완료")
 *   성공 (메시지 + 데이터)   → ApiResponse.ok("처리 완료", data)
 *   실패                    → ApiResponse.fail("오류 메시지")
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE) // 외부에서 직접 생성 금지, 팩토리 메서드 사용
@JsonInclude(JsonInclude.Include.NON_NULL)              // null 필드는 JSON에서 제외
public class ApiResponse<T> {

    private final boolean success; // 요청 성공 여부
    private final String  message; // 성공/실패 메시지
    private final T       data;    // 응답 데이터 (없으면 null)

    /** 데이터만 반환하는 성공 응답 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    /** 메시지 + 데이터를 반환하는 성공 응답 */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** 메시지만 반환하는 성공 응답 (데이터 없음) */
    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /** 실패 응답 */
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
