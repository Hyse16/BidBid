package com.auction.domain.bid.service;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.entity.AuctionItem.AuctionStatus;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.bid.dto.BidBroadcastMessage;
import com.auction.domain.bid.dto.BidRequest;
import com.auction.domain.bid.dto.BidResponse;
import com.auction.domain.bid.entity.Bid;
import com.auction.domain.bid.entity.Bid.BidStatus;
import com.auction.domain.bid.repository.BidRepository;
import com.auction.domain.user.entity.User;
import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import com.auction.domain.notification.service.NotificationService;
import com.auction.infra.redis.RedissonLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 입찰 서비스 — 실시간 경매의 핵심 기능
 *
 * ── 동시성 제어 ──────────────────────────────────────────────────────────────────
 * Redis 분산 락(Redisson)을 사용하여 동일 경매에 대한 동시 입찰 Race Condition을 방지한다.
 * 락 키: "auction:bid:{auctionId}"
 * 설정 : waitTime 3초 / leaseTime 5초 (RedissonLockService 기본값)
 *
 * ── 입찰 처리 흐름 ───────────────────────────────────────────────────────────────
 * 1. Redis 분산 락 획득
 * 2. 경매 상태 검증 (ACTIVE 여부)
 * 3. 입찰 유효성 검증 (소유자 입찰 방지 / 입찰가 / 최소 입찰 단위)
 * 4. 기존 WINNING 입찰 → OUTBID 처리
 * 5. 신규 입찰 저장
 * 6. 경매 현재가 갱신
 * 7. 락 해제
 * 8. WebSocket 브로드캐스트 (/topic/auction/{id})
 * 9. 텔레그램 알림 전송 (비동기 — 메인 스레드 비차단)
 *
 * ── 주의사항 ──────────────────────────────────────────────────────────────────────
 * 락 해제 → 트랜잭션 커밋 사이에 짧은 공백이 있다.
 * 프로덕션에서는 TransactionSynchronizationManager로 커밋 후 락 해제를 권장한다.
 * 포트폴리오 수준에서는 해당 공백이 수 밀리초 이하이므로 현재 구조로 충분하다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final BidRepository         bidRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final RedissonLockService   redissonLockService;
    private final SimpMessagingTemplate messagingTemplate;   // WebSocket 브로드캐스트
    private final NotificationService   notificationService; // DB 저장 + 텔레그램 전송

    private static final String LOCK_KEY_PREFIX = "auction:bid:"; // 분산 락 키 접두사

    // ── 입찰 처리 ─────────────────────────────────────────────────────────────────

    /**
     * 입찰을 처리한다 (분산 락 적용).
     *
     * 캐시 무효화: 입찰 성공 시 현재가가 변경되므로
     * "auction"(단건) 및 "auctions"(목록) 캐시를 모두 evict한다.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "auction",  key = "#auctionItemId"),
            @CacheEvict(value = "auctions", allEntries = true)
    })
    public BidResponse placeBid(Long auctionItemId, User bidder, BidRequest request) {
        String lockKey = LOCK_KEY_PREFIX + auctionItemId;

        return redissonLockService.executeWithLock(lockKey,
                () -> processBid(auctionItemId, bidder, request));
    }

    // ── 입찰 히스토리 조회 ────────────────────────────────────────────────────────

    /**
     * 특정 경매의 입찰 히스토리를 최신순으로 조회한다.
     *
     * 캐시 미적용: 실시간 데이터이므로 항상 DB에서 최신 값을 읽는다.
     */
    public List<BidResponse> getBidHistory(Long auctionItemId) {
        if (!auctionItemRepository.existsById(auctionItemId)) {
            throw new CustomException(ErrorCode.AUCTION_NOT_FOUND);
        }

        return bidRepository.findByAuctionItemIdWithUser(auctionItemId).stream()
                .map(BidResponse::from)
                .toList();
    }

    // ── private: 핵심 처리 로직 ───────────────────────────────────────────────────

    /**
     * 분산 락 내에서 실행되는 입찰 처리 로직.
     *
     * 이 메서드는 동일 경매에 대해 항상 단일 스레드로만 실행이 보장된다.
     */
    private BidResponse processBid(Long auctionItemId, User bidder, BidRequest request) {
        // 경매 조회 및 ACTIVE 상태 검증
        AuctionItem item = findActiveAuction(auctionItemId);

        // 입찰 유효성 검사
        validateBid(item, bidder, request.bidPrice());

        // 기존 WINNING 입찰 → OUTBID 처리 (밀려난 입찰자 반환)
        User outbidUser = markCurrentWinnerAsOutbid(auctionItemId);

        // 신규 입찰 저장
        Bid newBid = Bid.builder()
                .auctionItem(item)
                .user(bidder)
                .bidPrice(request.bidPrice())
                .build();
        bidRepository.save(newBid);

        // 경매 현재가 갱신
        item.updateCurrentPrice(request.bidPrice());

        // WebSocket 실시간 브로드캐스트
        broadcastBidUpdate(item.getId(), request.bidPrice(), bidder.getNickname());

        // 알림 전송: DB 기록 + 텔레그램 (비동기 — 메인 스레드 차단 없음)
        sendNotifications(item, bidder, outbidUser, request.bidPrice());

        return BidResponse.from(newBid);
    }

    /**
     * ACTIVE 상태의 경매를 조회한다.
     * 종료됐거나 대기 중인 경매에는 입찰할 수 없다.
     */
    private AuctionItem findActiveAuction(Long auctionItemId) {
        AuctionItem item = auctionItemRepository.findById(auctionItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        if (item.getStatus() != AuctionStatus.ACTIVE) {
            throw new CustomException(ErrorCode.AUCTION_NOT_ACTIVE);
        }

        return item;
    }

    /**
     * 입찰 유효성을 검사한다.
     *
     * 1. 판매자 본인 입찰 방지
     * 2. 입찰가 > 현재가
     * 3. 입찰가 >= 현재가 + 최소 입찰 단위
     */
    private void validateBid(AuctionItem item, User bidder, Long bidPrice) {
        // 판매자 본인 입찰 방지
        if (item.getUser().getId().equals(bidder.getId())) {
            throw new CustomException(ErrorCode.AUCTION_OWNER_CANNOT_BID);
        }

        // 현재가 이하 입찰 방지
        if (bidPrice <= item.getCurrentPrice()) {
            throw new CustomException(ErrorCode.BID_PRICE_TOO_LOW);
        }

        // 최소 입찰 단위 미충족 (현재가 + 최소단위 이상이어야 함)
        if (bidPrice < item.getCurrentPrice() + item.getMinBidUnit()) {
            throw new CustomException(ErrorCode.BID_UNIT_INVALID);
        }
    }

    /**
     * 현재 WINNING 입찰을 OUTBID으로 변경하고, 해당 입찰자를 반환한다.
     *
     * @return 밀려난 사용자 (최초 입찰인 경우 null)
     */
    private User markCurrentWinnerAsOutbid(Long auctionItemId) {
        return bidRepository
                .findFirstByAuctionItemIdAndStatusOrderByBidPriceDesc(auctionItemId, BidStatus.WINNING)
                .map(bid -> {
                    bid.markAsOutbid();
                    return bid.getUser();
                })
                .orElse(null); // 최초 입찰이면 null
    }

    /** WebSocket으로 입찰 갱신 정보를 모든 구독자에게 브로드캐스트한다 */
    private void broadcastBidUpdate(Long auctionItemId, Long currentPrice, String bidderNickname) {
        BidBroadcastMessage message = new BidBroadcastMessage(
                auctionItemId,
                currentPrice,
                bidderNickname,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/auction/" + auctionItemId, message);
        log.debug("입찰 브로드캐스트 완료: auctionId={}, price={}원", auctionItemId, currentPrice);
    }

    /**
     * NotificationService를 통해 알림을 전송한다 (DB 기록 + 텔레그램).
     *
     * 새 입찰자    : 📈 BID 알림
     * 밀려난 입찰자: ⚠️ OUTBID 알림 (최초 입찰이면 outbidUser == null, 생략)
     */
    private void sendNotifications(AuctionItem item, User bidder, User outbidUser, Long bidPrice) {
        notificationService.sendBidNotification(bidder.getId(), item.getTitle(), bidPrice);

        if (outbidUser != null) {
            notificationService.sendOutbidNotification(outbidUser.getId(), item.getTitle(), bidPrice);
        }
    }
}
