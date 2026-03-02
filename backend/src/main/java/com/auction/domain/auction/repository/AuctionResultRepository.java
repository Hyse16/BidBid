package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 경매 결과 레포지토리
 *
 * 경매 종료 스케줄러에서 낙찰 결과를 저장하고,
 * 사용자 마이페이지에서 낙찰 내역을 조회할 때 사용한다.
 */
public interface AuctionResultRepository extends JpaRepository<AuctionResult, Long> {

    /** 특정 경매의 낙찰 결과 조회 */
    Optional<AuctionResult> findByAuctionItemId(Long auctionItemId);
}
