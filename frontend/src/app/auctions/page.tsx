"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import apiClient from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { AuctionListResponse, AuctionSearchParams, CategoryResponse, PageResponse } from "@/types/auction";
import AuctionCard from "@/components/auction/AuctionCard";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

/**
 * 경매 목록 페이지
 *
 * 카테고리 / 상태 / 키워드 필터와 페이징을 제공한다.
 */
export default function AuctionsPage() {
  const [params, setParams] = useState<AuctionSearchParams>({
    page: 0,
    size: 12,
  });

  // 카테고리 목록 조회
  const { data: categoriesData } = useQuery<ApiResponse<CategoryResponse[]>>({
    queryKey: ["categories"],
    queryFn: () => apiClient.get("/api/categories").then((r) => r.data),
    staleTime: 60 * 60 * 1000, // 1시간 캐시
  });

  // 경매 목록 조회
  const { data: auctionsData, isLoading } = useQuery<ApiResponse<PageResponse<AuctionListResponse>>>({
    queryKey: ["auctions", params],
    queryFn: () =>
      apiClient
        .get("/api/auctions", { params })
        .then((r) => r.data),
  });

  const categories = categoriesData?.data ?? [];
  const auctions = auctionsData?.data;

  const updateFilter = (key: keyof AuctionSearchParams, value: string | number | undefined) => {
    setParams((prev) => ({ ...prev, [key]: value || undefined, page: 0 }));
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">경매 목록</h1>

      {/* 필터 영역 */}
      <div className="flex flex-wrap gap-3 mb-6">
        {/* 카테고리 필터 */}
        <select
          className="input-field w-auto"
          value={params.categoryId ?? ""}
          onChange={(e) => updateFilter("categoryId", e.target.value ? Number(e.target.value) : undefined)}
        >
          <option value="">전체 카테고리</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>

        {/* 상태 필터 */}
        <select
          className="input-field w-auto"
          value={params.status ?? ""}
          onChange={(e) => updateFilter("status", e.target.value || undefined)}
        >
          <option value="">전체 상태</option>
          <option value="ACTIVE">진행 중</option>
          <option value="PENDING">대기 중</option>
          <option value="ENDED">종료</option>
        </select>

        {/* 키워드 검색 */}
        <input
          type="text"
          className="input-field w-auto"
          placeholder="키워드 검색"
          defaultValue={params.keyword ?? ""}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              updateFilter("keyword", (e.target as HTMLInputElement).value);
            }
          }}
        />
      </div>

      {/* 경매 목록 */}
      {isLoading ? (
        <div className="py-20">
          <LoadingSpinner size="lg" />
        </div>
      ) : auctions && auctions.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {auctions.content.map((auction) => (
              <AuctionCard key={auction.id} auction={auction} />
            ))}
          </div>

          {/* 페이징 */}
          {auctions.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-8">
              <button
                className="btn-secondary px-3 py-1 text-sm"
                disabled={params.page === 0}
                onClick={() => setParams((p) => ({ ...p, page: (p.page ?? 1) - 1 }))}
              >
                이전
              </button>
              <span className="py-1 px-3 text-sm text-gray-600">
                {(params.page ?? 0) + 1} / {auctions.totalPages}
              </span>
              <button
                className="btn-secondary px-3 py-1 text-sm"
                disabled={(params.page ?? 0) >= auctions.totalPages - 1}
                onClick={() => setParams((p) => ({ ...p, page: (p.page ?? 0) + 1 }))}
              >
                다음
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-20 text-gray-500">
          <p className="text-lg">경매 상품이 없습니다</p>
          <p className="text-sm mt-1">검색 조건을 변경하거나 새 경매를 등록해 보세요</p>
        </div>
      )}
    </div>
  );
}
