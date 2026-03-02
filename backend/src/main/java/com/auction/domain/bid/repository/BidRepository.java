package com.auction.domain.bid.repository;

import com.auction.domain.bid.entity.Bid;
import com.auction.domain.bid.entity.Bid.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 입찰 레포지토리
 *
 * 주요 쿼리:
 *   - WINNING 입찰 조회   : 분산 락 내에서 현재 최고 입찰자를 OUTBID 처리할 때 사용
 *   - 히스토리 조회       : 입찰 이력 목록 (입찰자 닉네임 포함, fetch join)
 *   - 존재 여부 확인      : 경매 수정 가능 여부 판단 (AuctionService에서 사용)
 */
public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * 특정 경매의 현재 WINNING 입찰을 입찰가 내림차순으로 조회한다.
     *
     * 분산 락 내에서 기존 최고 입찰자를 OUTBID 처리할 때 사용한다.
     * 정상 운영 시 WINNING 입찰은 경매당 최대 1개이므로 첫 번째를 반환한다.
     */
    Optional<Bid> findFirstByAuctionItemIdAndStatusOrderByBidPriceDesc(
            Long auctionItemId, BidStatus status);

    /**
     * 특정 경매의 입찰 히스토리를 최신순으로 조회한다 (입찰자 정보 fetch join).
     *
     * N+1 방지를 위해 입찰자(User)를 fetch join으로 함께 로드한다.
     */
    @Query("SELECT b FROM Bid b JOIN FETCH b.user WHERE b.auctionItem.id = :auctionItemId ORDER BY b.createdAt DESC")
    List<Bid> findByAuctionItemIdWithUser(Long auctionItemId);

    /**
     * 특정 경매에 입찰 기록이 존재하는지 확인한다.
     *
     * AuctionService.hasBids()에서 경매 수정 가능 여부를 판단할 때 사용한다.
     */
    boolean existsByAuctionItemId(Long auctionItemId);

    /**
     * 특정 경매의 고유 입찰자 목록을 조회한다 (특정 사용자 제외).
     *
     * 경매 종료 시 낙찰자를 제외한 나머지 입찰자에게 LOSE 알림을 전송할 때 사용한다.
     */
    @Query("SELECT DISTINCT b.user FROM Bid b WHERE b.auctionItem.id = :auctionItemId AND b.user.id <> :excludeUserId")
    List<com.auction.domain.user.entity.User> findDistinctBiddersByAuctionItemIdExcluding(
            Long auctionItemId, Long excludeUserId);

    /**
     * 특정 경매의 고유 입찰자 목록을 조회한다 (전체).
     *
     * 만료 임박 알림 스케줄러에서 해당 경매 입찰자들에게 EXPIRY_WARNING을 전송할 때 사용한다.
     */
    @Query("SELECT DISTINCT b.user FROM Bid b WHERE b.auctionItem.id = :auctionItemId")
    List<com.auction.domain.user.entity.User> findDistinctBiddersByAuctionItemId(Long auctionItemId);

    /**
     * 특정 사용자의 입찰 목록을 최신순으로 조회한다 (경매 상품 + 사용자 fetch join).
     *
     * 마이페이지 "내 입찰 목록"에서 사용한다.
     * N+1 방지를 위해 auctionItem과 user를 fetch join으로 함께 로드한다.
     * BidResponse.from()에서 bid.getUser().getNickname()과 bid.getAuctionItem().getId()를 모두 사용한다.
     */
    @Query("SELECT b FROM Bid b JOIN FETCH b.auctionItem JOIN FETCH b.user WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Bid> findByUserIdOrderByCreatedAtDesc(Long userId);
}
