package com.auction.domain.watchlist.entity;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.user.entity.User;
import com.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 관심 목록 엔티티 (watchlist 테이블)
 *
 * 사용자가 관심 있는 경매 상품을 등록하는 매핑 테이블이다.
 * (user_id, auction_item_id) 조합에 UNIQUE 제약 조건으로 중복 등록을 방지한다.
 *
 * createdAt은 BaseEntity에서 JPA Auditing으로 자동 설정된다.
 */
@Entity
@Table(
        name = "watchlist",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_watchlist_user_auction",
                columnNames = {"user_id", "auction_item_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Watchlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 관심 목록 등록 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id", nullable = false)
    private AuctionItem auctionItem; // 관심 경매 상품
}
