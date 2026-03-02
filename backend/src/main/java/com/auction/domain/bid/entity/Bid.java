package com.auction.domain.bid.entity;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.user.entity.User;
import com.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 입찰 엔티티 (bids 테이블)
 *
 * 사용자가 경매에 입찰한 내역을 기록한다.
 * 새 입찰 성공 시 기존 WINNING 입찰은 OUTBID으로 자동 변경된다.
 *
 * 상태 전이:
 *   WINNING  → 현재 최고 입찰 (한 경매에 하나만 존재)
 *   OUTBID   → 더 높은 입찰로 밀려난 상태
 *   FAILED   → 분산 락 내 유효성 검사 실패 시 (현재 미사용, 확장용)
 *
 * 주의: BidService에서 분산 락을 걸고 처리하므로
 *       동시에 여러 WINNING 입찰이 생기는 Race Condition은 발생하지 않는다.
 */
@Entity
@Table(name = "bids")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Bid extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 연관 관계 ─────────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id", nullable = false)
    private AuctionItem auctionItem; // 입찰 대상 경매

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 입찰자

    // ── 입찰 정보 ─────────────────────────────────────────────────────────────────

    @Column(nullable = false)
    private Long bidPrice; // 입찰가

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BidStatus status = BidStatus.WINNING; // 초기 상태: 최고 입찰

    // ── 도메인 메서드 ─────────────────────────────────────────────────────────────

    /** 더 높은 입찰이 들어올 때 현재 입찰을 OUTBID으로 변경한다 */
    public void markAsOutbid() {
        this.status = BidStatus.OUTBID;
    }

    // ── 상태 열거형 ───────────────────────────────────────────────────────────────

    /** 입찰 상태 */
    public enum BidStatus {
        WINNING, // 현재 최고 입찰
        OUTBID,  // 상위 입찰로 인해 밀려남
        FAILED   // 유효성 검사 실패 (확장용)
    }
}
