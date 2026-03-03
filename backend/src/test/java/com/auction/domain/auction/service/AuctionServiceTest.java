package com.auction.domain.auction.service;

import com.auction.domain.auction.dto.*;
import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.entity.AuctionItem.AuctionStatus;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.bid.repository.BidRepository;
import com.auction.domain.category.entity.Category;
import com.auction.domain.category.repository.CategoryRepository;
import com.auction.domain.user.entity.User;
import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import com.auction.infra.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * AuctionService 단위 테스트
 *
 * 경매 CRUD의 핵심 비즈니스 규칙을 검증한다:
 *   - 경매 등록 (카테고리 존재 여부 검증)
 *   - 단건 조회 (캐시 레이어는 통합 테스트에서 검증)
 *   - 수정 (소유자 검증, 입찰 존재 시 수정 불가)
 *   - 삭제 (소유자 검증)
 */
@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock private AuctionItemRepository auctionItemRepository;
    @Mock private CategoryRepository    categoryRepository;
    @Mock private BidRepository         bidRepository;
    @Mock private S3Service             s3Service;

    @InjectMocks
    private AuctionService auctionService;

    // ── 테스트 픽스처 ─────────────────────────────────────────────────────────────

    private User     seller;
    private User     otherUser;
    private Category category;
    private AuctionItem auction;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(1L).email("seller@test.com").password("encoded")
                .nickname("판매자").role(User.Role.USER).build();

        otherUser = User.builder()
                .id(2L).email("other@test.com").password("encoded")
                .nickname("다른사용자").role(User.Role.USER).build();

        category = Category.builder()
                .id(1L).name("전자제품").build();

        auction = AuctionItem.builder()
                .id(10L)
                .user(seller)
                .category(category)
                .title("맥북 프로")
                .description("M3 맥북 프로 14인치")
                .startPrice(1_000_000L)
                .currentPrice(1_000_000L)
                .minBidUnit(10_000L)
                .status(AuctionStatus.PENDING)
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    // ── 경매 단건 조회 ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("경매 단건 조회")
    class GetAuction {

        @Test
        @DisplayName("존재하는 경매를 ID로 조회하면 응답 DTO를 반환한다")
        void getAuction_success() {
            // given
            given(auctionItemRepository.findByIdWithImages(10L)).willReturn(Optional.of(auction));

            // when
            AuctionResponse response = auctionService.getAuction(10L);

            // then
            assertThat(response.title()).isEqualTo("맥북 프로");
            assertThat(response.currentPrice()).isEqualTo(1_000_000L);
            assertThat(response.sellerNickname()).isEqualTo("판매자");
        }

        @Test
        @DisplayName("존재하지 않는 경매 조회 시 AUCTION_NOT_FOUND 예외가 발생한다")
        void getAuction_notFound() {
            // given
            given(auctionItemRepository.findByIdWithImages(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> auctionService.getAuction(999L))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_NOT_FOUND);
        }
    }

    // ── 경매 수정 ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("경매 수정")
    class UpdateAuction {

        private AuctionUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new AuctionUpdateRequest(
                    1L, "수정된 맥북 프로", "업데이트된 설명",
                    null, 15_000L,
                    LocalDateTime.now().plusHours(2),
                    LocalDateTime.now().plusDays(14)
            );
        }

        @Test
        @DisplayName("소유자가 입찰 없는 경매를 수정하면 성공한다")
        void updateAuction_success() {
            // given
            given(auctionItemRepository.findByIdWithImages(10L)).willReturn(Optional.of(auction));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(bidRepository.existsByAuctionItemId(10L)).willReturn(false); // 입찰 없음

            // when
            AuctionResponse response = auctionService.updateAuction(10L, seller, updateRequest, null);

            // then
            assertThat(response.title()).isEqualTo("수정된 맥북 프로");
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 수정 시 AUCTION_NOT_OWNER 예외가 발생한다")
        void updateAuction_notOwner() {
            // given
            given(auctionItemRepository.findByIdWithImages(10L)).willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> auctionService.updateAuction(10L, otherUser, updateRequest, null))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_NOT_OWNER);
        }

        @Test
        @DisplayName("입찰이 있는 경매 수정 시 AUCTION_CANNOT_MODIFY 예외가 발생한다")
        void updateAuction_hasBids() {
            // given
            given(auctionItemRepository.findByIdWithImages(10L)).willReturn(Optional.of(auction));
            given(bidRepository.existsByAuctionItemId(10L)).willReturn(true); // 입찰 있음

            // when & then
            assertThatThrownBy(() -> auctionService.updateAuction(10L, seller, updateRequest, null))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_CANNOT_MODIFY);
        }
    }

    // ── 경매 삭제 ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("경매 삭제")
    class DeleteAuction {

        @Test
        @DisplayName("소유자가 경매를 삭제하면 S3 이미지도 함께 삭제된다")
        void deleteAuction_success() {
            // given
            given(auctionItemRepository.findByIdWithImages(10L)).willReturn(Optional.of(auction));
            willDoNothing().given(auctionItemRepository).delete(auction);

            // when
            auctionService.deleteAuction(10L, seller);

            // then
            then(auctionItemRepository).should().delete(auction);
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 삭제 시 AUCTION_NOT_OWNER 예외가 발생한다")
        void deleteAuction_notOwner() {
            // given
            given(auctionItemRepository.findByIdWithImages(10L)).willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> auctionService.deleteAuction(10L, otherUser))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUCTION_NOT_OWNER);

            // 삭제 호출 안됨 검증
            then(auctionItemRepository).should(never()).delete(any());
        }
    }
}
