package com.auction.domain.user.controller;

import com.auction.domain.auction.dto.AuctionListResponse;
import com.auction.domain.bid.dto.BidResponse;
import com.auction.domain.user.dto.UserResponse;
import com.auction.domain.user.dto.UserUpdateRequest;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import com.auction.global.response.ApiResponse;
import com.auction.global.security.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 프로필 컨트롤러
 *
 * 엔드포인트:
 *   GET  /api/users/me          → 내 프로필 조회
 *   PUT  /api/users/me          → 내 프로필 수정
 *   GET  /api/users/me/auctions → 내가 등록한 경매 목록
 *   GET  /api/users/me/bids     → 내가 입찰한 경매 목록
 *
 * 모든 엔드포인트는 JWT 인증이 필요하다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final UserService               userService;
    private final CustomUserDetailsService  userDetailsService;

    // ── 내 프로필 ─────────────────────────────────────────────────────────────────

    /** GET /api/users/me — 내 프로필 조회 */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyProfile(user.getId())));
    }

    /** PUT /api/users/me — 내 프로필 수정 */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateMyProfile(user.getId(), request)));
    }

    // ── 내 경매 / 입찰 목록 ────────────────────────────────────────────────────────

    /** GET /api/users/me/auctions — 내가 등록한 경매 목록 */
    @GetMapping("/me/auctions")
    public ResponseEntity<ApiResponse<List<AuctionListResponse>>> getMyAuctions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyAuctions(user.getId())));
    }

    /** GET /api/users/me/bids — 내가 입찰한 경매 목록 */
    @GetMapping("/me/bids")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getMyBids(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyBids(user.getId())));
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /** UserDetails(Spring Security) → User 엔티티 변환 */
    private User resolveUser(UserDetails userDetails) {
        return userDetailsService.loadUserEntityByEmail(userDetails.getUsername());
    }
}
