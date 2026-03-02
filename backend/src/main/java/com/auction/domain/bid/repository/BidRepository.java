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
}
