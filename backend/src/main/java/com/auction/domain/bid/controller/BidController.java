package com.auction.domain.bid.controller;

import com.auction.domain.bid.dto.BidRequest;
import com.auction.domain.bid.dto.BidResponse;
import com.auction.domain.bid.service.BidService;
import com.auction.domain.user.entity.User;
import com.auction.global.response.ApiResponse;
import com.auction.global.security.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 입찰 컨트롤러
 *
 * REST API와 WebSocket STOMP 메시지 핸들러를 함께 제공한다.
 *
 * ── REST API ─────────────────────────────────────────────────────────────────────
 *   POST /api/auctions/{auctionId}/bids  : 입찰 (인증 필요)
 *   GET  /api/auctions/{auctionId}/bids  : 입찰 히스토리 (공개)
 *
 * ── WebSocket STOMP ───────────────────────────────────────────────────────────────
 *   SEND      /app/auction/{id}/bid      : 입찰 전송 (STOMP 연결 시 JWT 헤더 필요)
 *   SUBSCRIBE /topic/auction/{id}        : 실시간 입찰 결과 수신
 *
 * 프론트엔드 WebSocket 연결 예시 (SockJS + STOMP.js):
 *   const socket = new SockJS('/ws');
 *   const stomp  = Stomp.over(socket);
 *   stomp.connect({'Authorization': 'Bearer ' + token}, () => {
 *     stomp.subscribe('/topic/auction/1', msg => console.log(JSON.parse(msg.body)));
 *   });
 */
@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService               bidService;
    private final CustomUserDetailsService userDetailsService;

    // ── REST API ──────────────────────────────────────────────────────────────────

    /**
     * 입찰 (REST)
     *
     * POST /api/auctions/{auctionId}/bids
     * Body: {"bidPrice": 50000}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BidRequest request) {

        User user = resolveUser(userDetails);
        BidResponse response = bidService.placeBid(auctionId, user, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("입찰이 완료되었습니다", response));
    }

    /**
     * 입찰 히스토리 조회 (공개)
     *
     * GET /api/auctions/{auctionId}/bids
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBidHistory(
            @PathVariable Long auctionId) {

        return ResponseEntity.ok(ApiResponse.ok(bidService.getBidHistory(auctionId)));
    }

    // ── WebSocket STOMP ───────────────────────────────────────────────────────────

    /**
     * WebSocket을 통한 입찰 처리
     *
     * 클라이언트가 /app/auction/{id}/bid 로 메시지를 보내면
     * 입찰 처리 후 결과를 /topic/auction/{id} 로 자동 브로드캐스트한다.
     *
     * STOMP 헤더에 JWT 토큰이 포함되어야 인증이 통과된다.
     * (WebSocket Security 설정 필요 — 현재는 REST 방식이 주 경로)
     */
    @MessageMapping("/auction/{auctionId}/bid")
    public void placeBidViaWebSocket(
            @DestinationVariable Long auctionId,
            BidRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return; // 미인증 요청 무시

        User user = resolveUser(userDetails);
        bidService.placeBid(auctionId, user, request);
        // 브로드캐스트는 BidService.broadcastBidUpdate()에서 /topic/auction/{id}로 자동 전송
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /** UserDetails에서 도메인 User 엔티티로 변환한다 */
    private User resolveUser(UserDetails userDetails) {
        return userDetailsService.loadUserEntityByEmail(userDetails.getUsername());
    }
}
