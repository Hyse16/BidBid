package com.auction.domain.category.controller;

import com.auction.domain.category.dto.CategoryResponse;
import com.auction.domain.category.service.CategoryService;
import com.auction.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 카테고리 컨트롤러
 *
 * 카테고리 목록 조회 API를 제공한다.
 * 비로그인 사용자도 접근 가능하다 (SecurityConfig에서 GET /api/categories/** 허용).
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 전체 카테고리 목록 조회
     *
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllCategories()));
    }
}
