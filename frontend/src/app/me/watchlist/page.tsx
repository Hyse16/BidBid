"use client";

import Link from "next/link";
import Image from "next/image";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import apiClient from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { WatchlistResponse } from "@/types/auction";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import StatusBadge from "@/components/ui/StatusBadge";
import { formatPrice, formatRelativeTime } from "@/lib/format";

/**
 * 마이페이지 — 관심 목록
 */
export default function WatchlistPage() {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery<ApiResponse<WatchlistResponse[]>>({
    queryKey: ["watchlist"],
    queryFn: () => apiClient.get("/api/watchlist").then((r) => r.data),
  });

  // 관심 목록 제거 뮤테이션
  const removeMutation = useMutation({
    mutationFn: (auctionId: number) =>
      apiClient.delete(`/api/watchlist/${auctionId}`).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["watchlist"] });
    },
  });

  const watchlist = data?.data ?? [];

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">관심 목록</h1>

      {isLoading ? (
        <div className="py-20">
          <LoadingSpinner size="lg" />
        </div>
      ) : watchlist.length > 0 ? (
        <div className="space-y-3">
          {watchlist.map((item) => (
            <div key={item.watchlistId} className="card flex gap-4 p-4">
              {/* 썸네일 */}
              <div className="relative w-20 h-20 flex-shrink-0 bg-gray-100 rounded-lg overflow-hidden">
                {item.thumbnailUrl ? (
                  <Image
                    src={item.thumbnailUrl}
                    alt={item.title}
                    fill
                    className="object-cover"
                    sizes="80px"
                  />
                ) : (
                  <div className="flex items-center justify-center h-full text-gray-300 text-xs">
                    No Image
                  </div>
                )}
              </div>

              {/* 경매 정보 */}
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <Link
                    href={`/auctions/${item.auctionId}`}
                    className="font-semibold text-gray-900 hover:text-blue-600 truncate"
                  >
                    {item.title}
                  </Link>
                  <StatusBadge status={item.status} />
                </div>

                <p className="text-xs text-blue-500 mt-0.5">{item.categoryName}</p>

                <div className="flex items-center gap-4 mt-2">
                  <span className="font-bold text-blue-600">{formatPrice(item.currentPrice)}</span>
                  {item.status === "ACTIVE" && (
                    <span className="text-xs text-gray-400">
                      {formatRelativeTime(item.endAt)} 마감
                    </span>
                  )}
                </div>
              </div>

              {/* 제거 버튼 */}
              <button
                onClick={() => removeMutation.mutate(item.auctionId)}
                disabled={removeMutation.isPending}
                className="text-gray-400 hover:text-red-500 transition-colors self-start"
                title="관심 목록에서 제거"
              >
                ✕
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20">
          <p className="text-gray-500">관심 목록이 비어있습니다</p>
          <Link href="/auctions" className="btn-primary inline-block mt-4 text-sm">
            경매 둘러보기
          </Link>
        </div>
      )}
    </div>
  );
}
