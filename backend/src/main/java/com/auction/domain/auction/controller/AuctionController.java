package com.auction.domain.auction.controller;

import com.auction.domain.auction.dto.*;
import com.auction.domain.auction.service.AuctionService;
import com.auction.domain.user.entity.User;
import com.auction.global.response.ApiResponse;
import com.auction.global.security.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 경매 상품 컨트롤러
 *
 * 공개 API (GET 조회)는 인증 없이 접근 가능하고,
 * 등록 / 수정 / 삭제는 JWT 인증이 필요하다.
 *
 * multipart/form-data 구조:
 *   request : JSON 형태의 요청 데이터 (@RequestPart("request"))
 *   images  : 이미지 파일 목록         (@RequestPart("images"), 선택)
 *
 * SecurityConfig에서 GET /api/auctions/** 를 permitAll로 설정했으므로
 * 조회 메서드에는 @AuthenticationPrincipal을 사용하지 않는다.
 */
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService           auctionService;
    private final CustomUserDetailsService userDetailsService; // UserDetails → User 엔티티 변환

    // ── 공개 API ─────────────────────────────────────────────────────────────────

    /**
     * 경매 목록 조회 (필터 + 페이징)
     *
     * GET /api/auctions?categoryId=1&status=ACTIVE&keyword=맥북&page=0&size=12
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionListResponse>>> getAuctions(
            @ModelAttribute AuctionSearchCondition condition,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(auctionService.getAuctions(condition, pageable)));
    }

    /**
     * 경매 단건 상세 조회
     *
     * GET /api/auctions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuctionResponse>> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionService.getAuction(id)));
    }

    /**
     * 경매 낙찰 결과 조회
     *
     * GET /api/auctions/{id}/result
     *
     * 낙찰자가 있으면 AuctionResultResponse를 반환하고,
     * 유찰(입찰 없이 종료)이면 data=null로 응답한다.
     */
    @GetMapping("/{id}/result")
    public ResponseEntity<ApiResponse<AuctionResultResponse>> getAuctionResult(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionService.getAuctionResult(id)));
    }

    // ── 인증 필요 API ─────────────────────────────────────────────────────────────

    /**
     * 경매 등록 (multipart/form-data)
     *
     * POST /api/auctions
     * Content-Type: multipart/form-data
     *   request : {"categoryId":1, "title":"...", ...} (JSON)
     *   images  : [file1, file2, ...] (선택)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuctionResponse>> createAuction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart("request") AuctionCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        User user = resolveUser(userDetails);
        AuctionResponse response = auctionService.createAuction(user, request, images);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("경매가 등록되었습니다", response));
    }

    /**
     * 경매 수정 (소유자만 가능, multipart/form-data)
     *
     * PUT /api/auctions/{id}
     * Content-Type: multipart/form-data
     *   request : {"categoryId":1, "title":"...", ...} (JSON)
     *   images  : [file1, file2, ...] (없으면 기존 이미지 유지)
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuctionResponse>> updateAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart("request") AuctionUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        User user = resolveUser(userDetails);
        AuctionResponse response = auctionService.updateAuction(id, user, request, images);

        return ResponseEntity.ok(ApiResponse.ok("경매가 수정되었습니다", response));
    }

    /**
     * 경매 삭제 (소유자만 가능)
     *
     * DELETE /api/auctions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        auctionService.deleteAuction(id, user);

        return ResponseEntity.ok(ApiResponse.ok("경매가 삭제되었습니다"));
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /** UserDetails에서 도메인 User 엔티티로 변환한다 */
    private User resolveUser(UserDetails userDetails) {
        return userDetailsService.loadUserEntityByEmail(userDetails.getUsername());
    }
}
