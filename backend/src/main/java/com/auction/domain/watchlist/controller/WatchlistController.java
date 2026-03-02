package com.auction.domain.watchlist.controller;

import com.auction.domain.user.entity.User;
import com.auction.domain.watchlist.dto.WatchlistResponse;
import com.auction.domain.watchlist.service.WatchlistService;
import com.auction.global.response.ApiResponse;
import com.auction.global.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관심 목록 컨트롤러
 *
 * 엔드포인트:
 *   POST   /api/watchlist/{auctionId} → 관심 목록 추가
 *   DELETE /api/watchlist/{auctionId} → 관심 목록 제거
 *   GET    /api/watchlist             → 내 관심 목록 조회
 *
 * 모든 엔드포인트는 JWT 인증이 필요하다.
 */
@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final WatchlistService         watchlistService;
    private final CustomUserDetailsService userDetailsService;

    // ── 관심 목록 추가 / 제거 ───────────────────────────────────────────────────────

    /** POST /api/watchlist/{auctionId} — 관심 목록 추가 */
    @PostMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<Void>> addToWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long auctionId) {
        User user = resolveUser(userDetails);
        watchlistService.addToWatchlist(user.getId(), auctionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /** DELETE /api/watchlist/{auctionId} — 관심 목록 제거 */
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long auctionId) {
        User user = resolveUser(userDetails);
        watchlistService.removeFromWatchlist(user.getId(), auctionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── 내 관심 목록 조회 ─────────────────────────────────────────────────────────

    /** GET /api/watchlist — 내 관심 목록 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WatchlistResponse>>> getMyWatchlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success(watchlistService.getMyWatchlist(user.getId())));
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /** UserDetails(Spring Security) → User 엔티티 변환 */
    private User resolveUser(UserDetails userDetails) {
        return userDetailsService.loadUserEntityByEmail(userDetails.getUsername());
    }
}
