package com.auction.domain.category.entity;

import com.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 경매 카테고리 엔티티 (categories 테이블)
 *
 * 경매 상품을 분류하는 카테고리 정보를 나타낸다.
 * 예) 전자기기, 의류, 도서, 스포츠 등
 *
 * 카테고리는 관리자가 사전 등록하며 일반 사용자는 수정 불가하다.
 */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 카테고리 이름 (예: 전자기기, 의류)
}
