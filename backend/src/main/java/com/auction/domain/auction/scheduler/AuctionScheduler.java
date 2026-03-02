package com.auction.domain.auction.scheduler;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.entity.AuctionItem.AuctionStatus;
import com.auction.domain.auction.entity.AuctionResult;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.auction.repository.AuctionResultRepository;
import com.auction.domain.bid.entity.Bid;
import com.auction.domain.bid.entity.Bid.BidStatus;
import com.auction.domain.bid.repository.BidRepository;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 경매 스케줄러
 *
 * 두 가지 정기 작업을 수행한다:
 *
 * ── processExpiredAuctions (매 1분) ──────────────────────────────────────────────
 * ACTIVE 상태이고 endAt이 지난 경매를 ENDED로 전환한다.
 * 낙찰 결과(AuctionResult)를 생성하고 낙찰자/미낙찰자에게 텔레그램 알림을 전송한다.
 * 입찰자가 없는 경우(유찰)는 AuctionResult 없이 ENDED 처리만 한다.
 *
 * ── sendExpiryWarnings (매 1분) ───────────────────────────────────────────────────
 * 1시간 이내 종료 예정인 ACTIVE 경매의 입찰자들에게 마감 임박 알림을 전송한다.
 * 1분 단위로 실행되므로 endAt이 [now+1h, now+1h+1min] 범위인 경매만 조회한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final AuctionItemRepository   auctionItemRepository;
    private final AuctionResultRepository auctionResultRepository;
    private final BidRepository           bidRepository;
    private final NotificationService     notificationService;

    // ── 경매 종료 처리 ────────────────────────────────────────────────────────────

    /**
     * 만료된 경매를 ENDED 상태로 전환하고 낙찰 결과를 생성한다.
     *
     * 처리 흐름:
     *   1. ACTIVE + endAt < now 인 경매 목록 조회
     *   2. 상태 → ENDED
     *   3. 최고 입찰(WINNING) 조회
     *   4. 낙찰자 있으면 → AuctionResult 저장 + WIN/LOSE 알림
     *   5. 낙찰자 없으면 → 유찰 처리 (ENDED만, 결과 없음)
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void processExpiredAuctions() {
        List<AuctionItem> expiredList = auctionItemRepository
                .findByStatusAndEndAtBefore(AuctionStatus.ACTIVE, LocalDateTime.now());

        if (expiredList.isEmpty()) return;

        log.info("경매 종료 처리 시작: {}건", expiredList.size());

        for (AuctionItem item : expiredList) {
            processExpiredAuction(item);
        }
    }

    /** 단일 만료 경매를 처리한다 */
    private void processExpiredAuction(AuctionItem item) {
        // 상태 전환: ACTIVE → ENDED
        item.changeStatus(AuctionStatus.ENDED);

        // 최고 입찰 조회
        Optional<Bid> winningBidOpt = bidRepository
                .findFirstByAuctionItemIdAndStatusOrderByBidPriceDesc(item.getId(), BidStatus.WINNING);

        if (winningBidOpt.isEmpty()) {
            // 유찰: 입찰자 없이 종료
            log.info("유찰 처리: auctionId={}, title={}", item.getId(), item.getTitle());
            return;
        }

        Bid   winningBid = winningBidOpt.get();
        User  winner     = winningBid.getUser();
        Long  finalPrice = winningBid.getBidPrice();

        // 낙찰 결과 저장
        AuctionResult result = AuctionResult.builder()
                .auctionItem(item)
                .winner(winner)
                .finalPrice(finalPrice)
                .build();
        auctionResultRepository.save(result);

        // 낙찰자에게 WIN 알림 (비동기)
        notificationService.sendWinNotification(winner.getId(), item.getTitle(), finalPrice);

        // 나머지 입찰자에게 LOSE 알림 (비동기)
        List<User> losers = bidRepository
                .findDistinctBiddersByAuctionItemIdExcluding(item.getId(), winner.getId());
        losers.forEach(loser ->
                notificationService.sendLoseNotification(loser.getId(), item.getTitle(), finalPrice));

        log.info("낙찰 처리 완료: auctionId={}, winner={}, finalPrice={}원",
                item.getId(), winner.getNickname(), finalPrice);
    }

    // ── 만료 임박 알림 ────────────────────────────────────────────────────────────

    /**
     * 1시간 내 종료 예정 경매의 입찰자들에게 만료 임박 알림을 전송한다.
     *
     * 조회 범위: endAt ∈ [now+1h, now+1h+1min]
     * → 1분 단위 실행에서 중복 알림 없이 딱 한 번만 발송되도록 범위를 제한한다.
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional(readOnly = true)
    public void sendExpiryWarnings() {
        LocalDateTime now           = LocalDateTime.now();
        LocalDateTime oneHourLater  = now.plusHours(1);
        LocalDateTime oneHourOneMins = now.plusHours(1).plusMinutes(1);

        List<AuctionItem> soonEndingList = auctionItemRepository
                .findByStatusAndEndAtBetween(AuctionStatus.ACTIVE, oneHourLater, oneHourOneMins);

        if (soonEndingList.isEmpty()) return;

        log.info("만료 임박 알림 대상: {}건", soonEndingList.size());

        for (AuctionItem item : soonEndingList) {
            // 해당 경매에 입찰한 사용자 전체에게 알림 (비동기)
            List<User> bidders = bidRepository.findDistinctBiddersByAuctionItemId(item.getId());

            bidders.forEach(user ->
                    notificationService.sendExpiryWarningNotification(
                            user.getId(), item.getTitle(), item.getCurrentPrice()));

            log.info("만료 임박 알림 전송: auctionId={}, 대상={}명", item.getId(), bidders.size());
        }
    }
}
