package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.entity.AuctionItem.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    Optional<AuctionItem> findByIdWithImages(Long id);

    /**
     * 경매 종료 스케줄러용: ACTIVE 상태이고 endAt이 지난 경매 목록 조회.
     * 매 1분마다 실행되어 해당 경매를 ENDED로 전환한다.
     */
    List<AuctionItem> findByStatusAndEndAtBefore(AuctionStatus status, LocalDateTime endAt);

    /**
     * 만료 임박 알림 스케줄러용: ACTIVE 상태이고 endAt이 특정 시간 범위 내인 경매.
     * 1시간 전 마감 임박 알림 전송 대상을 찾을 때 사용한다.
     */
    List<AuctionItem> findByStatusAndEndAtBetween(AuctionStatus status, LocalDateTime start, LocalDateTime end);
}
