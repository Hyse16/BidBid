"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import apiClient from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { AuctionListResponse } from "@/types/auction";
import AuctionCard from "@/components/auction/AuctionCard";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

/**
 * 마이페이지 — 내가 등록한 경매 목록
 */
export default function MyAuctionsPage() {
  const { data, isLoading } = useQuery<ApiResponse<AuctionListResponse[]>>({
    queryKey: ["my-auctions"],
    queryFn: () => apiClient.get("/api/users/me/auctions").then((r) => r.data),
  });

  const auctions = data?.data ?? [];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-bold text-gray-900">내 경매</h1>
        <Link href="/auctions/new" className="btn-primary text-sm">
          + 경매 등록
        </Link>
      </div>

      {isLoading ? (
        <div className="py-20">
          <LoadingSpinner size="lg" />
        </div>
      ) : auctions.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {auctions.map((auction) => (
            <AuctionCard key={auction.id} auction={auction} />
          ))}
        </div>
      ) : (
        <div className="text-center py-20">
          <p className="text-gray-500">등록한 경매가 없습니다</p>
          <Link href="/auctions/new" className="btn-primary inline-block mt-4 text-sm">
            첫 경매 등록하기
          </Link>
        </div>
      )}
    </div>
  );
}
