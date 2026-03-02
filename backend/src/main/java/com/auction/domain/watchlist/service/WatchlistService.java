package com.auction.domain.watchlist.service;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
import com.auction.domain.watchlist.dto.WatchlistResponse;
import com.auction.domain.watchlist.entity.Watchlist;
import com.auction.domain.watchlist.repository.WatchlistRepository;
import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관심 목록 서비스
 *
 * 사용자가 관심 있는 경매를 추가/제거하고, 관심 목록을 조회하는 기능을 제공한다.
 * 동일 경매 중복 등록 시 WATCHLIST_ALREADY_EXISTS 예외를 발생시킨다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchlistService {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final WatchlistRepository   watchlistRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final UserRepository        userRepository;

    // ── 관심 목록 추가 / 제거 ───────────────────────────────────────────────────────

    /**
     * 관심 목록에 경매를 추가한다.
     *
     * 이미 추가된 경우 WATCHLIST_ALREADY_EXISTS 예외를 발생시킨다.
     */
    @Transactional
    public void addToWatchlist(Long userId, Long auctionItemId) {
        // 중복 등록 방지
        if (watchlistRepository.existsByUserIdAndAuctionItemId(userId, auctionItemId)) {
            throw new CustomException(ErrorCode.WATCHLIST_ALREADY_EXISTS);
        }

        // User + AuctionItem 프록시 참조 (FK만 필요)
        User        user    = userRepository.getReferenceById(userId);
        AuctionItem auction = auctionItemRepository.findById(auctionItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        watchlistRepository.save(Watchlist.builder()
                .user(user)
                .auctionItem(auction)
                .build());
    }

    /**
     * 관심 목록에서 경매를 제거한다.
     *
     * 등록되지 않은 항목을 삭제하려 할 경우 WATCHLIST_NOT_FOUND 예외를 발생시킨다.
     */
    @Transactional
    public void removeFromWatchlist(Long userId, Long auctionItemId) {
        if (!watchlistRepository.existsByUserIdAndAuctionItemId(userId, auctionItemId)) {
            throw new CustomException(ErrorCode.WATCHLIST_NOT_FOUND);
        }

        watchlistRepository.deleteByUserIdAndAuctionItemId(userId, auctionItemId);
    }

    // ── 관심 목록 조회 ────────────────────────────────────────────────────────────

    /** 내 관심 목록을 최신 등록순으로 조회한다 */
    public List<WatchlistResponse> getMyWatchlist(Long userId) {
        return watchlistRepository.findByUserIdWithAuction(userId)
                .stream()
                .map(WatchlistResponse::from)
                .toList();
    }
}
