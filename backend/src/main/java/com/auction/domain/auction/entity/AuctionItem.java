package com.auction.domain.auction.entity;

import com.auction.domain.category.entity.Category;
import com.auction.domain.user.entity.User;
import com.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 경매 상품 엔티티 (auction_items 테이블)
 *
 * 경매 플랫폼의 핵심 엔티티로, 판매자가 등록한 경매 상품 정보를 나타낸다.
 *
 * 상태(status) 전이 흐름:
 *   PENDING → ACTIVE  : 스케줄러가 startAt 도래 시 자동 전환 (Step 5)
 *   ACTIVE  → ENDED   : 스케줄러가 endAt 경과 시 자동 전환 (Step 5)
 *   ACTIVE  → CANCELLED : 관리자 또는 판매자가 취소
 *
 * 입찰 처리 시 currentPrice를 갱신하며, 이 값이 최고 입찰가를 반영한다.
 * 이미지는 CascadeType.ALL로 AuctionItem과 생명주기를 함께 한다.
 */
@Entity
@Table(name = "auction_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class AuctionItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 연관 관계 ─────────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 판매자 (경매 등록자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 경매 카테고리

    @OneToMany(mappedBy = "auctionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AuctionImage> images = new ArrayList<>(); // 경매 이미지 목록

    // ── 상품 정보 ─────────────────────────────────────────────────────────────────

    @Column(nullable = false, length = 100)
    private String title; // 상품 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 상품 설명

    // ── 가격 정보 ─────────────────────────────────────────────────────────────────

    @Column(nullable = false)
    private Long startPrice; // 시작가

    @Column(nullable = false)
    private Long currentPrice; // 현재가 (입찰 성공 시 갱신)

    @Column
    private Long buyNowPrice; // 즉시 구매가 (null이면 즉구 불가)

    @Column(nullable = false)
    private Long minBidUnit; // 최소 입찰 단위 (예: 1000원)

    // ── 경매 상태 / 기간 ──────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.PENDING; // 기본 상태: 대기 중

    @Column(nullable = false)
    private LocalDateTime startAt; // 경매 시작 일시

    @Column(nullable = false)
    private LocalDateTime endAt; // 경매 종료 일시

    // ── 도메인 메서드 ─────────────────────────────────────────────────────────────

    /**
     * 경매 기본 정보를 수정한다.
     * 입찰이 없는 PENDING 상태에서만 호출해야 한다 (서비스 계층에서 검증).
     */
    public void update(String title, String description, Category category,
                       Long buyNowPrice, Long minBidUnit,
                       LocalDateTime startAt, LocalDateTime endAt) {
        this.title       = title;
        this.description = description;
        this.category    = category;
        this.buyNowPrice = buyNowPrice;
        this.minBidUnit  = minBidUnit;
        this.startAt     = startAt;
        this.endAt       = endAt;
    }

    /** 입찰 성공 시 현재가를 갱신한다 */
    public void updateCurrentPrice(Long price) {
        this.currentPrice = price;
    }

    /** 경매 상태를 변경한다 (스케줄러 또는 관리자가 호출) */
    public void changeStatus(AuctionStatus status) {
        this.status = status;
    }

    // ── 상태 열거형 ───────────────────────────────────────────────────────────────

    /** 경매 상태 */
    public enum AuctionStatus {
        PENDING,    // 대기 중 (시작 전)
        ACTIVE,     // 진행 중
        ENDED,      // 종료됨
        CANCELLED   // 취소됨
    }
}
