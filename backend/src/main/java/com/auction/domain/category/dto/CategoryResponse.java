package com.auction.domain.category.dto;

import com.auction.domain.category.entity.Category;

/**
 * 카테고리 응답 DTO
 *
 * 카테고리 목록 조회 API에서 반환되는 데이터를 담는다.
 * record를 사용하여 불변 객체로 선언한다.
 */
public record CategoryResponse(
        Long   id,   // 카테고리 ID
        String name  // 카테고리 이름
) {
    /** Category 엔티티로부터 DTO를 생성한다 */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
