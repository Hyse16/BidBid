package com.auction.domain.watchlist.repository;

import com.auction.domain.watchlist.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 관심 목록 레포지토리
 *
 * 주요 쿼리:
 *   - 사용자별 목록 조회  : 마이페이지 관심 목록 표시용
 *   - 중복 확인          : 같은 경매를 다시 추가하는 경우 예외 처리
 *   - 삭제              : 사용자 + 경매 조건으로 항목 삭제
 */
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    /**
     * 특정 사용자의 관심 목록을 등록순으로 조회한다 (경매 + 카테고리 + 이미지 fetch join).
     *
     * N+1 방지를 위해 auctionItem, category, images를 함께 로드한다.
     * DISTINCT로 이미지 컬렉션 조인으로 인한 중복 엔티티를 제거한다.
     */
    @Query("SELECT DISTINCT w FROM Watchlist w JOIN FETCH w.auctionItem a JOIN FETCH a.category LEFT JOIN FETCH a.images WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Watchlist> findByUserIdWithAuction(Long userId);

    /** 이미 관심 목록에 추가된 항목인지 확인한다 */
    boolean existsByUserIdAndAuctionItemId(Long userId, Long auctionItemId);

    /** 사용자 + 경매 조건으로 관심 목록 항목을 삭제한다 */
    void deleteByUserIdAndAuctionItemId(Long userId, Long auctionItemId);
}
