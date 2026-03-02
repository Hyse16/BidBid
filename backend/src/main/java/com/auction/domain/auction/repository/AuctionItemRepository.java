package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 경매 상품 레포지토리
 *
 * JpaRepository 기본 메서드와 QueryDSL 커스텀 인터페이스를 함께 상속한다.
 * 동적 검색은 AuctionItemRepositoryImpl(QueryDSL)에서 처리한다.
 */
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long>, AuctionItemRepositoryCustom {

    /**
     * 경매 상품 단건을 이미지 목록과 함께 조회한다 (N+1 방지).
     *
     * 상세 조회 시 이미지를 fetch join으로 한 번에 로드하여
     * LazyInitializationException 및 N+1 문제를 방지한다.
     */
    @Query("SELECT a FROM AuctionItem a LEFT JOIN FETCH a.images WHERE a.id = :id")
    java.util.Optional<AuctionItem> findByIdWithImages(Long id);
}
