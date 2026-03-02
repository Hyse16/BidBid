package com.auction.domain.auction.dto;

/**
 * 경매 목록 검색 조건 DTO
 *
 * 쿼리 파라미터로 전달되어 QueryDSL 동적 쿼리에 사용된다.
 * 모든 필드는 nullable이며, null인 조건은 쿼리에서 자동으로 제외된다.
 *
 * 사용 예시:
 *   GET /api/auctions?categoryId=1&status=ACTIVE&keyword=맥북
 *
 * @param categoryId 카테고리 ID 필터 (null이면 전체 카테고리)
 * @param status     경매 상태 필터 (null이면 전체 상태, 예: PENDING/ACTIVE/ENDED/CANCELLED)
 * @param keyword    제목 키워드 검색 (null 또는 빈 문자열이면 전체)
 */
public record AuctionSearchCondition(
        Long   categoryId, // 카테고리 필터
        String status,     // 경매 상태 필터
        String keyword     // 제목 검색 키워드
) {}
