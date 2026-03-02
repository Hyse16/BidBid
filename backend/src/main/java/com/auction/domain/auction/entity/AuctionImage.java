package com.auction.domain.auction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 경매 상품 이미지 엔티티 (auction_images 테이블)
 *
 * 경매 상품에 첨부된 S3 이미지 정보를 관리한다.
 * 첫 번째로 업로드된 이미지가 썸네일로 지정된다 (isThumbnail=true).
 *
 * AuctionItem.images 컬렉션에 속하며, AuctionItem 삭제 시
 * CascadeType.ALL + orphanRemoval=true 로 함께 제거된다.
 *
 * 주의: createdAt이 필요한 경우 BaseEntity를 상속할 수 있으나,
 * 이미지 엔티티는 단순 저장 목적으로 독립 필드로 관리한다.
 */
@Entity
@Table(name = "auction_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class AuctionImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id", nullable = false)
    private AuctionItem auctionItem; // 소속 경매 상품

    @Column(nullable = false, length = 500)
    private String imageUrl; // S3에 저장된 이미지 전체 URL

    @Column(nullable = false)
    @Builder.Default
    private Boolean isThumbnail = false; // 썸네일 여부 (경매 목록 카드에 표시)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 이미지 업로드 일시

    /** 정적 팩토리: 이미지 엔티티 생성 (첫 번째 이미지를 썸네일로 지정) */
    public static AuctionImage of(AuctionItem item, String url, boolean isThumbnail) {
        return AuctionImage.builder()
                .auctionItem(item)
                .imageUrl(url)
                .isThumbnail(isThumbnail)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
