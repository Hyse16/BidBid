"use client";

import { useCallback, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import Image from "next/image";
import { useParams, useRouter } from "next/navigation";
import apiClient from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { AuctionResponse } from "@/types/auction";
import { BidBroadcastMessage, BidRequest, BidResponse } from "@/types/bid";
import { useWebSocket } from "@/hooks/useWebSocket";
import { useAuthStore } from "@/store/authStore";
import StatusBadge from "@/components/ui/StatusBadge";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { formatDateTime, formatPrice } from "@/lib/format";

/**
 * 경매 상세 페이지 — 프리미엄 다크 테마
 *
 * WebSocket으로 실시간 입찰 업데이트를 수신하고
 * 현재가를 골드 컬러로 즉시 반영한다.
 */
export default function AuctionDetailPage() {
  const params    = useParams();
  const router    = useRouter();
  const queryClient = useQueryClient();
  const { isLoggedIn, user } = useAuthStore();
  const auctionId = Number(params.id);

  const [currentPrice,   setCurrentPrice]   = useState<number | null>(null);
  const [recentBidder,   setRecentBidder]   = useState<string | null>(null);
  const [bidAmount,      setBidAmount]      = useState("");
  const [bidError,       setBidError]       = useState("");
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);

  // 경매 상세 조회
  const { data: auctionData, isLoading } = useQuery<ApiResponse<AuctionResponse>>({
    queryKey: ["auction", auctionId],
    queryFn: () => apiClient.get(`/api/auctions/${auctionId}`).then((r) => r.data),
  });

  // 입찰 히스토리 조회
  const { data: bidHistoryData } = useQuery<ApiResponse<BidResponse[]>>({
    queryKey: ["bids", auctionId],
    queryFn: () => apiClient.get(`/api/auctions/${auctionId}/bids`).then((r) => r.data),
  });

  // WebSocket 실시간 입찰 업데이트 수신
  const handleBidUpdate = useCallback(
    (message: BidBroadcastMessage) => {
      setCurrentPrice(message.currentPrice);
      setRecentBidder(message.bidderNickname);
      queryClient.invalidateQueries({ queryKey: ["bids",    auctionId] });
      queryClient.invalidateQueries({ queryKey: ["auction", auctionId] });
    },
    [auctionId, queryClient]
  );

  const { connected } = useWebSocket(auctionId, handleBidUpdate);

  // 입찰 뮤테이션
  const bidMutation = useMutation({
    mutationFn: (request: BidRequest) =>
      apiClient
        .post<ApiResponse<BidResponse>>(`/api/auctions/${auctionId}/bids`, request)
        .then((r) => r.data),
    onSuccess: () => {
      setBidAmount("");
      setBidError("");
    },
    onError: (error: unknown) => {
      const axiosError = error as { response?: { data?: { message?: string } } };
      setBidError(axiosError?.response?.data?.message ?? "입찰에 실패했습니다.");
    },
  });

  // 관심 목록 토글 뮤테이션
  const watchlistMutation = useMutation({
    mutationFn: () =>
      apiClient.post(`/api/watchlist/${auctionId}`).then((r) => r.data),
  });

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!auctionData?.data) {
    return (
      <div className="text-center py-20">
        <p className="text-gray-500">경매를 찾을 수 없습니다</p>
      </div>
    );
  }

  const auction      = auctionData.data;
  const displayPrice = currentPrice ?? auction.currentPrice;
  const bidHistory   = bidHistoryData?.data ?? [];
  const isActive     = auction.status === "ACTIVE";
  const isOwner      = user?.nickname === auction.sellerNickname;
  const minNextBid   = displayPrice + auction.minBidUnit;

  const handleBid = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isLoggedIn) {
      router.push("/login");
      return;
    }
    const price = Number(bidAmount);
    if (isNaN(price) || price <= 0) {
      setBidError("올바른 입찰가를 입력해주세요.");
      return;
    }
    bidMutation.mutate({ bidPrice: price });
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      {/* ── 이미지 영역 ─────────────────────────────────── */}
      <div>
        <div className="relative w-full h-80 bg-[#12121f] rounded-xl overflow-hidden
                        border border-[rgba(212,175,55,0.15)]">
          {auction.images.length > 0 ? (
            <Image
              src={auction.images[selectedImageIndex].url}
              alt={auction.title}
              fill
              className="object-contain"
              sizes="(max-width: 1024px) 100vw, 50vw"
            />
          ) : (
            <div className="flex flex-col items-center justify-center h-full text-gray-700 gap-2">
              <svg className="w-16 h-16 opacity-20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M13.293 2.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l8-8z"/>
              </svg>
              <span className="text-sm opacity-30">이미지 없음</span>
            </div>
          )}
        </div>

        {/* 썸네일 목록 */}
        {auction.images.length > 1 && (
          <div className="flex gap-2 mt-3">
            {auction.images.map((img, idx) => (
              <button
                key={idx}
                onClick={() => setSelectedImageIndex(idx)}
                className={`relative w-16 h-16 rounded-lg overflow-hidden border-2 transition-all duration-200 ${
                  selectedImageIndex === idx
                    ? "border-[#f59e0b] shadow-[0_0_8px_rgba(212,175,55,0.4)]"
                    : "border-[rgba(212,175,55,0.1)] hover:border-[rgba(212,175,55,0.3)]"
                }`}
              >
                <Image
                  src={img.url}
                  alt={`이미지 ${idx + 1}`}
                  fill
                  className="object-cover"
                  sizes="64px"
                />
              </button>
            ))}
          </div>
        )}
      </div>

      {/* ── 경매 정보 영역 ───────────────────────────────── */}
      <div className="space-y-5">
        {/* 기본 정보 */}
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-[10px] text-gray-600 uppercase tracking-widest font-medium">
              {auction.categoryName}
            </span>
            <StatusBadge status={auction.status} />
            {connected && isActive && (
              <span className="flex items-center gap-1.5 text-xs text-[#d4af37]">
                <span className="w-1.5 h-1.5 bg-[#f59e0b] rounded-full animate-pulse
                                 shadow-[0_0_6px_rgba(212,175,55,0.8)]" />
                실시간 연결
              </span>
            )}
          </div>
          <h1 className="text-2xl font-bold text-gray-100 leading-snug">{auction.title}</h1>
          <p className="text-sm text-gray-600 mt-1">판매자: {auction.sellerNickname}</p>
        </div>

        {/* ── 가격 정보 카드 ───────────────────────────── */}
        <div className="bg-[#1a1a2e] rounded-xl border border-[rgba(212,175,55,0.2)]
                        p-5 space-y-3 shadow-[0_4px_24px_rgba(0,0,0,0.4)]">
          {/* 현재가 — 메인 강조 */}
          <div className="flex justify-between items-center">
            <span className="text-gray-500 text-sm tracking-wide">현재가</span>
            <span className="text-4xl font-bold text-[#f59e0b] tracking-tight
                             drop-shadow-[0_0_12px_rgba(245,158,11,0.4)]">
              {formatPrice(displayPrice)}
            </span>
          </div>

          {recentBidder && (
            <p className="text-xs text-[#d4af37] text-right opacity-80">
              ↑ {recentBidder}님이 입찰
            </p>
          )}

          <div className="border-t border-[rgba(212,175,55,0.1)] pt-3 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">시작가</span>
              <span className="text-gray-400">{formatPrice(auction.startPrice)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">최소 입찰 단위</span>
              <span className="text-gray-400">{formatPrice(auction.minBidUnit)}</span>
            </div>
            {auction.buyNowPrice && (
              <div className="flex justify-between text-sm">
                <span className="text-[#f59e0b] font-medium">즉시 구매가</span>
                <span className="text-[#f59e0b] font-medium">{formatPrice(auction.buyNowPrice)}</span>
              </div>
            )}
            <div className="flex justify-between text-sm border-t border-[rgba(212,175,55,0.08)] pt-2">
              <span className="text-gray-600">마감 시간</span>
              <span className="text-gray-400">{formatDateTime(auction.endAt)}</span>
            </div>
          </div>
        </div>

        {/* ── 입찰 폼 ─────────────────────────────────── */}
        {isActive && !isOwner && (
          <form onSubmit={handleBid} className="space-y-3">
            <div className="flex gap-2">
              <input
                type="number"
                className="input-field"
                placeholder={`최소 ${formatPrice(minNextBid)}`}
                value={bidAmount}
                onChange={(e) => setBidAmount(e.target.value)}
                min={minNextBid}
              />
              <button
                type="submit"
                className="btn-primary whitespace-nowrap px-6"
                disabled={bidMutation.isPending}
              >
                {bidMutation.isPending ? <LoadingSpinner size="sm" /> : "입찰하기"}
              </button>
            </div>
            {bidError && (
              <p className="text-red-400 text-sm">{bidError}</p>
            )}
            {!isLoggedIn && (
              <p className="text-gray-600 text-sm">입찰하려면 로그인이 필요합니다.</p>
            )}
          </form>
        )}

        {/* 관심 목록 추가 버튼 */}
        {isLoggedIn && !isOwner && (
          <button
            onClick={() => watchlistMutation.mutate()}
            className="btn-secondary w-full"
            disabled={watchlistMutation.isPending}
          >
            ♡ 관심 목록에 추가
          </button>
        )}

        {/* 상품 설명 */}
        <div className="bg-[#12121f] rounded-xl p-4 border border-[rgba(212,175,55,0.08)]">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">
            상품 설명
          </h2>
          <p className="text-gray-400 text-sm whitespace-pre-wrap leading-relaxed">
            {auction.description}
          </p>
        </div>
      </div>

      {/* ── 입찰 히스토리 (하단 전체 너비) ─────────────── */}
      <div className="lg:col-span-2">
        <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">
          입찰 내역
        </h2>
        {bidHistory.length > 0 ? (
          <div className="bg-[#1a1a2e] rounded-xl border border-[rgba(212,175,55,0.12)] overflow-hidden">
            {bidHistory.map((bid, idx) => (
              <div
                key={bid.id}
                className={`flex items-center justify-between px-4 py-3 transition-colors
                  ${idx === 0 ? "bg-[rgba(212,175,55,0.05)]" : "hover:bg-[rgba(212,175,55,0.03)]"}
                  ${idx !== 0 ? "border-t border-[rgba(212,175,55,0.07)]" : ""}`}
              >
                <div className="flex items-center gap-2">
                  {idx === 0 && (
                    <span className="text-[#f59e0b] text-xs">🏆</span>
                  )}
                  <span className="font-medium text-sm text-gray-300">
                    {bid.bidderNickname}
                  </span>
                  <span className="text-xs text-gray-700">
                    {formatDateTime(bid.createdAt)}
                  </span>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`font-bold ${idx === 0 ? "text-[#f59e0b]" : "text-gray-500"}`}>
                    {formatPrice(bid.bidPrice)}
                  </span>
                  {bid.status === "WINNING" && (
                    <span className="badge-active">최고가</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-700 text-sm py-6 text-center
                        bg-[#1a1a2e] rounded-xl border border-[rgba(212,175,55,0.08)]">
            아직 입찰 내역이 없습니다
          </p>
        )}
      </div>
    </div>
  );
}
