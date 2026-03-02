package com.auction.domain.auction.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 경매 수정 요청 DTO
 *
 * 소유자만 호출 가능하며, 입찰이 없는 상태에서만 수정이 허용된다.
 * startPrice는 수정 불가 (입찰 공정성 보장).
 * buyNowPrice를 null로 전달하면 즉시 구매 옵션이 제거된다.
 *
 * @param categoryId  변경할 카테고리 ID
 * @param title       변경할 제목
 * @param description 변경할 설명 (선택)
 * @param buyNowPrice 변경할 즉시 구매가 (null이면 즉구 옵션 제거)
 * @param minBidUnit  변경할 최소 입찰 단위
 * @param startAt     변경할 경매 시작 일시
 * @param endAt       변경할 경매 종료 일시 (현재 이후)
 */
public record AuctionUpdateRequest(

        @NotNull(message = "카테고리 ID는 필수입니다")
        Long categoryId,

        @NotBlank(message = "제목은 필수입니다")
        String title,

        String description, // 선택 항목

        Long buyNowPrice, // null이면 즉시 구매 옵션 제거

        @NotNull(message = "최소 입찰 단위는 필수입니다")
        @Min(value = 1, message = "최소 입찰 단위는 1원 이상이어야 합니다")
        Long minBidUnit,

        @NotNull(message = "시작 일시는 필수입니다")
        LocalDateTime startAt,

        @NotNull(message = "종료 일시는 필수입니다")
        @Future(message = "종료 일시는 현재 시각 이후여야 합니다")
        LocalDateTime endAt
) {}
