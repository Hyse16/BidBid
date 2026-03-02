package com.auction.domain.auction.entity;

import com.auction.domain.user.entity.User;
import com.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 경매 결과 엔티티 (auction_results 테이블)
 *
 * 경매 종료 시 스케줄러가 낙찰 정보를 기록한다.
 * 경매당 하나의 결과만 존재하므로 auctionItem에 UNIQUE 제약을 건다.
 *
 * 입찰자가 없는 경우 AuctionResult가 생성되지 않는다.
 * (유찰 처리 — AuctionItem.status = ENDED, AuctionResult 없음)
 */
@Entity
@Table(name = "auction_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class AuctionResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id", unique = true, nullable = false)
    private AuctionItem auctionItem; // 종료된 경매 (UNIQUE - 경매당 결과 1개)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id", nullable = false)
    private User winner; // 낙찰자

    @Column(nullable = false)
    private Long finalPrice; // 최종 낙찰가
}
