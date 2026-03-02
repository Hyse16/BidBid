package com.auction.domain.auction.dto;

import java.time.LocalDateTime;

/**
 * 경매 목록 응답 DTO (썸네일 이미지 포함)
 *
 * QueryDSL Projections.constructor()를 통해 DB에서 직접 매핑된다.
 * 목록 조회 성능을 위해 썸네일 URL만 포함하고 전체 이미지 목록은 제외한다.
 *
 * 필드 순서는 AuctionItemRepositoryImpl의 Projections.constructor() 인자 순서와 일치해야 한다.
 *
 * @param thumbnailUrl 썸네일 이미지 URL (이미지 없으면 null)
 */
public record AuctionListResponse(
        Long          id,           // 경매 ID
        String        title,        // 상품 제목
        Long          currentPrice, // 현재가
        Long          buyNowPrice,  // 즉시 구매가 (null이면 즉구 불가)
        String        status,       // 경매 상태 문자열
        LocalDateTime endAt,        // 종료 일시
        String        categoryName, // 카테고리 이름
        String        thumbnailUrl  // 썸네일 이미지 URL (null 가능)
) {}
