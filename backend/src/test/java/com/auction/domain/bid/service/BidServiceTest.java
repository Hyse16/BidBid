package com.auction.domain.bid.service;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.entity.AuctionItem.AuctionStatus;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.bid.dto.BidRequest;
import com.auction.domain.bid.dto.BidResponse;
import com.auction.domain.bid.entity.Bid;
import com.auction.domain.bid.entity.Bid.BidStatus;
import com.auction.domain.bid.repository.BidRepository;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.user.entity.User;
import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import com.auction.infra.redis.RedissonLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * BidService 단위 테스트
 *
 * 입찰 처리의 핵심 비즈니스 규칙을 검증한다:
 *   - 정상 입찰 처리 (분산 락 내부 로직)
 *   - 판매자 본인 입찰 방지
 *   - 현재가 이하 입찰 방지
 *   - 최소 입찰 단위 미충족 방지
 *   - 비활성 경매 입찰 방지
 *
 * RedissonLockService는 Callable을 바로 실행하는 stub으로 대체한다.
 */
@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock private BidRepository         bidRepository;
    @Mock private AuctionItemRepository auctionItemRepository;
    @Mock private RedissonLockService   redissonLockService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private NotificationService   notificationService;

    @InjectMocks
    private BidService bidService;

    // ── 테스트 픽스처 ─────────────────────────────────────────────────────────────

    private User    seller;   // 판매자
    private User    bidder;   // 입찰자
    private AuctionItem activeAuction;

    @BeforeEach
    void setUp() {
        // 판매자 (ID=1)
        seller = User.builder()
                .id(1L)
                .email("seller@test.com")
                .password("encoded")
                .nickname("판매자")
                .role(User.Role.USER)
                .build();

        // 입찰자 (ID=2)
        bidder = User.builder()
                .id(2L)
                .email("bidder@test.com")
                .password("encoded")
                .nickname("입찰자")
                .telegramChatId("12345")
                .role(User.Role.USER)
                .build();

        // ACTIVE 상태 경매: 시작가 10,000 / 최소단위 1,000
        activeAuction = AuctionItem.builder()
                .id(100L)
                .user(seller)
                .title("테스트 경매 상품")
                .startPrice(10_000L)
                .currentPrice(10_000L)
                .minBidUnit(1_000L)
                .status(AuctionStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusHours(1))
                .endAt(LocalDateTime.now().plusHours(23))
                .build();

        // RedissonLockService stub: 락 없이 Callable 바로 실행
        given(redissonLockService.executeWithLock(anyString(), any()))
                .willAnswer(inv -> ((Callable<?>) inv.getArgument(1)).call());
    }

    // ── 정상 입찰 테스트 ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("정상 입찰")
    class SuccessfulBid {

        @Test
        @DisplayName("최소 입찰 단위 이상의 금액으로 입찰 시 성공한다")
        void placeBid_success() {
            // given
            long bidPrice = 12_000L; // 현재가(10,000) + 최소단위(1,000) + α
            BidRequest request = new BidRequest(bidPrice);

            given(auctionItemRepository.findById(100L)).willReturn(Optional.of(activeAuction));
            given(bidRepository.findFirstByAuctionItemIdAndStatusOrderByBidPriceDesc(
                    100L, BidStatus.WINNING)).willReturn(Optional.empty()); // 기존 입찰 없음

            Bid savedBid = Bid.builder()
                    .id(1L)
                    .auctionItem(activeAuction)
                    .user(bidder)
                    .bidPrice(bidPrice)
                    .build();
            given(bidRepository.save(any(Bid.class))).willReturn(savedBid);

            // when
            BidResponse response = bidService.placeBid(100L, bidder, request);

            // then
            assertThat(response.bidPrice()).isEqualTo(bidPrice);
            assertThat(response.bidderNickname()).isEqualTo("입찰자");

            // WebSocket 브로드캐스트 및 알림 전송 확인
            then(messagingTemplate).should().convertAndSend(
                    eq("/topic/auction/100"), any(Object.class));
            then(notificationService).should()
                    .sendBidNotification(bidder.getId(), activeAuction.getTitle(), bidPrice);
        }

        @Test
        @DisplayName("기존 WINNING 입찰이 있으면 OUTBID 처리 후 새 입찰이 저장된다")
        void placeBid_outbidsPreviousWinner() {
            // given
            User previousBidder = User.builder().id(3L).nickname("이전입찰자").build();
            Bid previousWinning = Bid.builder()
                    .id(5L)
                    .user(previousBidder)
                    .auctionItem(activeAuction)
                    .bidPrice(11_000L)
                    .status(BidStatus.WINNING)
                    .build();

            given(auctionItemRepository.findById(100L)).willReturn(Optional.of(activeAuction));
            given(bidRepository.findFirstByAuctionItemIdAndStatusOrderByBidPriceDesc(
                    100L, BidStatus.WINNING)).willReturn(Optional.of(previousWinning));

            Bid newBid = Bid.builder()
                    .id(6L).user(bidder).auctionItem(activeAuction).bidPrice(13_000L).build();
            given(bidRepository.save(any(Bid.class))).willReturn(newBid);

            // when
            bidService.placeBid(100L, bidder, new BidRequest(13_000L));

            // then - 이전 입찰은 OUTBID으로 변경
            assertThat(previousWinning.getStatus()).isEqualTo(BidStatus.OUTBID);

            // OUTBID 알림도 전송
            then(notificationService).should()
                    .sendOutbidNotification(previousBidder.getId(), activeAuction.getTitle(), 13_000L);
        }
    }

    // ── 입찰 유효성 검사 실패 테스트 ────────────────────────────────────────────────

    @Nested
    @DisplayName("입찰 유효성 검사 실패")
    class BidValidationFailure {

        @Test
        @DisplayName("경매 소유자가 입찰하면 AUCTION_OWNER_CANNOT_BID 예외가 발생한다")
        void placeBid_ownerCannotBid() {
            // given
            given(auctionItemRepository.findById(100L)).willReturn(Optional.of(activeAuction));

            // when & then
            assertThatThrownBy(() -> bidService.placeBid(100L, seller, new BidRequest(12_000L)))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_OWNER_CANNOT_BID);
        }

        @Test
        @DisplayName("현재가 이하 입찰 시 BID_PRICE_TOO_LOW 예외가 발생한다")
        void placeBid_priceTooLow() {
            // given
            given(auctionItemRepository.findById(100L)).willReturn(Optional.of(activeAuction));

            // when & then (현재가 10,000 이하)
            assertThatThrownBy(() -> bidService.placeBid(100L, bidder, new BidRequest(10_000L)))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BID_PRICE_TOO_LOW);
        }

        @Test
        @DisplayName("최소 입찰 단위 미충족 시 BID_UNIT_INVALID 예외가 발생한다")
        void placeBid_unitInvalid() {
            // given
            given(auctionItemRepository.findById(100L)).willReturn(Optional.of(activeAuction));

            // when & then (현재가 10,000 + 최소단위 1,000 = 11,000 미만)
            assertThatThrownBy(() -> bidService.placeBid(100L, bidder, new BidRequest(10_500L)))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BID_UNIT_INVALID);
        }

        @Test
        @DisplayName("ACTIVE가 아닌 경매에 입찰하면 AUCTION_NOT_ACTIVE 예외가 발생한다")
        void placeBid_notActiveAuction() {
            // given - ENDED 상태 경매
            AuctionItem endedAuction = AuctionItem.builder()
                    .id(200L)
                    .user(seller)
                    .title("종료된 경매")
                    .startPrice(10_000L)
                    .currentPrice(10_000L)
                    .minBidUnit(1_000L)
                    .status(AuctionStatus.ENDED)
                    .startAt(LocalDateTime.now().minusDays(2))
                    .endAt(LocalDateTime.now().minusDays(1))
                    .build();

            given(auctionItemRepository.findById(200L)).willReturn(Optional.of(endedAuction));

            // when & then
            assertThatThrownBy(() -> bidService.placeBid(200L, bidder, new BidRequest(12_000L)))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_NOT_ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 경매에 입찰하면 AUCTION_NOT_FOUND 예외가 발생한다")
        void placeBid_auctionNotFound() {
            // given
            given(auctionItemRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bidService.placeBid(999L, bidder, new BidRequest(12_000L)))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_NOT_FOUND);
        }
    }

    // ── 입찰 히스토리 조회 테스트 ─────────────────────────────────────────────────

    @Nested
    @DisplayName("입찰 히스토리 조회")
    class BidHistory {

        @Test
        @DisplayName("경매 ID로 입찰 히스토리를 최신순으로 조회한다")
        void getBidHistory_success() {
            // given
            given(auctionItemRepository.existsById(100L)).willReturn(true);

            Bid bid1 = Bid.builder().id(1L).user(bidder).auctionItem(activeAuction)
                    .bidPrice(11_000L).build();
            Bid bid2 = Bid.builder().id(2L).user(bidder).auctionItem(activeAuction)
                    .bidPrice(12_000L).build();
            given(bidRepository.findByAuctionItemIdWithUser(100L)).willReturn(List.of(bid2, bid1));

            // when
            List<BidResponse> result = bidService.getBidHistory(100L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).bidPrice()).isEqualTo(12_000L); // 최신순
        }

        @Test
        @DisplayName("존재하지 않는 경매의 히스토리 조회 시 AUCTION_NOT_FOUND 예외가 발생한다")
        void getBidHistory_auctionNotFound() {
            // given
            given(auctionItemRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> bidService.getBidHistory(999L))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_NOT_FOUND);
        }
    }
}
