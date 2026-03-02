package com.auction.domain.user.service;

import com.auction.domain.auction.dto.AuctionListResponse;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.bid.dto.BidResponse;
import com.auction.domain.bid.repository.BidRepository;
import com.auction.domain.user.dto.UserResponse;
import com.auction.domain.user.dto.UserUpdateRequest;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 프로필 서비스
 *
 * 내 정보 조회 / 수정, 내 경매 목록, 내 입찰 목록 기능을 제공한다.
 * 서비스 레이어에서 User 엔티티를 직접 받지 않고, userId를 통해 조회하여
 * 트랜잭션 범위 안에서 영속 상태(managed)를 보장한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final UserRepository        userRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final BidRepository         bidRepository;

    // ── 프로필 조회 / 수정 ─────────────────────────────────────────────────────────

    /** 내 프로필 조회 */
    public UserResponse getMyProfile(Long userId) {
        User user = findUserById(userId);
        return UserResponse.from(user);
    }

    /**
     * 내 프로필 수정
     *
     * 닉네임 중복 시 NICKNAME_ALREADY_EXISTS 예외 발생.
     * 현재 닉네임과 동일한 경우 중복 체크를 건너뛴다.
     */
    @Transactional
    public UserResponse updateMyProfile(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);

        // 닉네임이 변경된 경우에만 중복 검사
        if (!user.getNickname().equals(request.nickname())
                && userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.updateProfile(request.nickname(), request.telegramChatId());
        return UserResponse.from(user);
    }

    // ── 내 경매 / 입찰 목록 ────────────────────────────────────────────────────────

    /** 내가 등록한 경매 목록 조회 (최신순) */
    public List<AuctionListResponse> getMyAuctions(Long userId) {
        return auctionItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** 내가 입찰한 경매 목록 조회 (최신순) */
    public List<BidResponse> getMyBids(Long userId) {
        return bidRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(BidResponse::from)
                .toList();
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
