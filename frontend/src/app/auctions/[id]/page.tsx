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
 * 경매 상세 페이지
 *
 * WebSocket으로 실시간 입찰 업데이트를 수신하고
 * 화면에 현재가를 즉시 반영한다.
 */
export default function AuctionDetailPage() {
  const params = useParams();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { isLoggedIn, user } = useAuthStore();
  const auctionId = Number(params.id);

  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [recentBidder, setRecentBidder] = useState<string | null>(null);
  const [bidAmount, setBidAmount] = useState("");
  const [bidError, setBidError] = useState("");
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
      // 입찰 히스토리 및 경매 캐시 무효화 → 자동 리패치
      queryClient.invalidateQueries({ queryKey: ["bids", auctionId] });
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
      <div className="py-20">
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

  const auction = auctionData.data;
  const displayPrice = currentPrice ?? auction.currentPrice;
  const bidHistory = bidHistoryData?.data ?? [];
  const isActive = auction.status === "ACTIVE";
  const isOwner = user?.nickname === auction.sellerNickname;
  const minNextBid = displayPrice + auction.minBidUnit;

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
      {/* 이미지 영역 */}
      <div>
        <div className="relative w-full h-80 bg-gray-100 rounded-xl overflow-hidden">
          {auction.images.length > 0 ? (
            <Image
              src={auction.images[selectedImageIndex].url}
              alt={auction.title}
              fill
              className="object-contain"
              sizes="(max-width: 1024px) 100vw, 50vw"
            />
          ) : (
            <div className="flex items-center justify-center h-full text-gray-400">
              이미지 없음
            </div>
          )}
        </div>

        {/* 썸네일 목록 */}
        {auction.images.length > 1 && (
          <div className="flex gap-2 mt-2">
            {auction.images.map((img, idx) => (
              <button
                key={idx}
                onClick={() => setSelectedImageIndex(idx)}
                className={`relative w-16 h-16 rounded-lg overflow-hidden border-2 transition-colors ${
                  selectedImageIndex === idx ? "border-blue-500" : "border-transparent"
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

      {/* 경매 정보 영역 */}
      <div className="space-y-6">
        {/* 기본 정보 */}
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-blue-600 text-sm font-medium">{auction.categoryName}</span>
            <StatusBadge status={auction.status} />
            {connected && isActive && (
              <span className="flex items-center gap-1 text-xs text-green-600">
                <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
                실시간 연결
              </span>
            )}
          </div>
          <h1 className="text-2xl font-bold text-gray-900">{auction.title}</h1>
          <p className="text-sm text-gray-500 mt-1">판매자: {auction.sellerNickname}</p>
        </div>

        {/* 가격 정보 */}
        <div className="card p-4 space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-gray-600">현재가</span>
            <span className="text-3xl font-bold text-blue-600">{formatPrice(displayPrice)}</span>
          </div>

          {recentBidder && (
            <p className="text-sm text-green-600 text-right">
              최근 입찰자: {recentBidder}
            </p>
          )}

          <div className="flex justify-between text-sm text-gray-500">
            <span>시작가</span>
            <span>{formatPrice(auction.startPrice)}</span>
          </div>

          <div className="flex justify-between text-sm text-gray-500">
            <span>최소 입찰 단위</span>
            <span>{formatPrice(auction.minBidUnit)}</span>
          </div>

          {auction.buyNowPrice && (
            <div className="flex justify-between text-sm">
              <span className="text-orange-600 font-medium">즉시 구매가</span>
              <span className="text-orange-600 font-medium">{formatPrice(auction.buyNowPrice)}</span>
            </div>
          )}

          <div className="flex justify-between text-sm text-gray-500 border-t pt-2">
            <span>마감 시간</span>
            <span>{formatDateTime(auction.endAt)}</span>
          </div>
        </div>

        {/* 입찰 폼 */}
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
                className="btn-primary whitespace-nowrap"
                disabled={bidMutation.isPending}
              >
                {bidMutation.isPending ? <LoadingSpinner size="sm" /> : "입찰하기"}
              </button>
            </div>

            {bidError && <p className="text-red-500 text-sm">{bidError}</p>}

            {!isLoggedIn && (
              <p className="text-gray-500 text-sm">입찰하려면 로그인이 필요합니다.</p>
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
        <div>
          <h2 className="font-semibold text-gray-900 mb-2">상품 설명</h2>
          <p className="text-gray-600 text-sm whitespace-pre-wrap">{auction.description}</p>
        </div>
      </div>

      {/* 입찰 히스토리 (하단 전체 너비) */}
      <div className="lg:col-span-2">
        <h2 className="font-semibold text-gray-900 mb-3">입찰 내역</h2>
        {bidHistory.length > 0 ? (
          <div className="card divide-y">
            {bidHistory.map((bid) => (
              <div key={bid.id} className="flex items-center justify-between p-3">
                <div>
                  <span className="font-medium text-sm">{bid.bidderNickname}</span>
                  <span className="text-xs text-gray-400 ml-2">
                    {formatDateTime(bid.createdAt)}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="font-bold text-blue-600">{formatPrice(bid.bidPrice)}</span>
                  {bid.status === "WINNING" && (
                    <span className="badge-active">최고가</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-400 text-sm">아직 입찰 내역이 없습니다.</p>
        )}
      </div>
    </div>
  );
}
