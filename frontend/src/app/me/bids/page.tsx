"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import apiClient from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { BidResponse } from "@/types/bid";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { formatDateTime, formatPrice } from "@/lib/format";

const BID_STATUS_LABEL: Record<string, string> = {
  WINNING: "최고 입찰",
  OUTBID:  "상위 입찰로 밀림",
  FAILED:  "입찰 실패",
};

const BID_STATUS_CLASS: Record<string, string> = {
  WINNING: "badge-active",
  OUTBID:  "badge-ended",
  FAILED:  "badge-cancelled",
};

/**
 * 마이페이지 — 내 입찰 내역
 */
export default function MyBidsPage() {
  const { data, isLoading } = useQuery<ApiResponse<BidResponse[]>>({
    queryKey: ["my-bids"],
    queryFn: () => apiClient.get("/api/users/me/bids").then((r) => r.data),
  });

  const bids = data?.data ?? [];

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">입찰 내역</h1>

      {isLoading ? (
        <div className="py-20">
          <LoadingSpinner size="lg" />
        </div>
      ) : bids.length > 0 ? (
        <div className="card divide-y">
          {bids.map((bid) => (
            <div key={bid.id} className="flex items-center justify-between p-4">
              <div>
                <Link
                  href={`/auctions/${bid.auctionItemId}`}
                  className="font-medium text-blue-600 hover:underline text-sm"
                >
                  경매 #{bid.auctionItemId}
                </Link>
                <p className="text-xs text-gray-400 mt-0.5">
                  {formatDateTime(bid.createdAt)}
                </p>
              </div>

              <div className="flex items-center gap-3">
                <span className="font-bold text-gray-900">{formatPrice(bid.bidPrice)}</span>
                <span className={BID_STATUS_CLASS[bid.status]}>
                  {BID_STATUS_LABEL[bid.status]}
                </span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20">
          <p className="text-gray-500">입찰 내역이 없습니다</p>
          <Link href="/auctions" className="btn-primary inline-block mt-4 text-sm">
            경매 둘러보기
          </Link>
        </div>
      )}
    </div>
  );
}
