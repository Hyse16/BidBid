package com.auction.domain.auction.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 경매 등록 요청 DTO
 *
 * multipart/form-data 요청의 "request" 파트(JSON)로 전달된다.
 * 이미지 파일은 "images" 파트(List<MultipartFile>)로 별도 전달한다.
 *
 * @param categoryId  경매 카테고리 ID
 * @param title       상품 제목
 * @param description 상품 설명 (선택)
 * @param startPrice  시작가 (최소 1원)
 * @param buyNowPrice 즉시 구매가 (null이면 즉구 옵션 없음)
 * @param minBidUnit  최소 입찰 단위 (최소 1원)
 * @param startAt     경매 시작 일시
 * @param endAt       경매 종료 일시 (현재 이후)
 */
public record AuctionCreateRequest(

        @NotNull(message = "카테고리 ID는 필수입니다")
        Long categoryId,

        @NotBlank(message = "제목은 필수입니다")
        String title,

        String description, // 선택 항목

        @NotNull(message = "시작가는 필수입니다")
        @Min(value = 1, message = "시작가는 1원 이상이어야 합니다")
        Long startPrice,

        Long buyNowPrice, // 즉시 구매가 (선택)

        @NotNull(message = "최소 입찰 단위는 필수입니다")
        @Min(value = 1, message = "최소 입찰 단위는 1원 이상이어야 합니다")
        Long minBidUnit,

        @NotNull(message = "시작 일시는 필수입니다")
        LocalDateTime startAt,

        @NotNull(message = "종료 일시는 필수입니다")
        @Future(message = "종료 일시는 현재 시각 이후여야 합니다")
        LocalDateTime endAt
) {}
