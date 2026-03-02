package com.auction.domain.category.repository;

import com.auction.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 카테고리 레포지토리
 *
 * 단순 CRUD만 사용하므로 JpaRepository 기본 메서드만 활용한다.
 * 목록 조회는 CategoryService에서 캐시(@Cacheable)와 함께 사용한다.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
