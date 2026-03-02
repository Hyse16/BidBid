package com.auction.domain.category.service;

import com.auction.domain.category.dto.CategoryResponse;
import com.auction.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 서비스
 *
 * 카테고리 목록은 자주 변경되지 않으므로 Redis 캐시를 적용한다.
 * TTL은 RedisConfig에서 "categories" 이름으로 1시간으로 설정된다.
 *
 * 카테고리 추가/수정 기능은 관리자 기능으로 별도 구현 예정이며,
 * 변경 시 @CacheEvict로 캐시를 제거해야 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 전체 카테고리 목록을 조회한다.
     *
     * "categories::all" 키로 캐싱하여 DB 조회를 최소화한다.
     * 카테고리가 변경되면 캐시를 수동으로 evict해야 한다.
     */
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
