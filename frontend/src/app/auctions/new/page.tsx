"use client";

import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import apiClient from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { AuctionResponse, CategoryResponse } from "@/types/auction";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useAuthStore } from "@/store/authStore";

/**
 * 경매 등록 페이지
 *
 * multipart/form-data로 이미지와 JSON 데이터를 함께 전송한다.
 */
export default function NewAuctionPage() {
  const router = useRouter();
  const { isLoggedIn } = useAuthStore();

  const [form, setForm] = useState({
    categoryId: "",
    title: "",
    description: "",
    startPrice: "",
    buyNowPrice: "",
    minBidUnit: "1000",
    startAt: "",
    endAt: "",
  });
  const [images, setImages] = useState<File[]>([]);
  const [error, setError] = useState("");

  // 카테고리 목록 조회
  const { data: categoriesData } = useQuery<ApiResponse<CategoryResponse[]>>({
    queryKey: ["categories"],
    queryFn: () => apiClient.get("/api/categories").then((r) => r.data),
  });

  // 경매 등록 뮤테이션
  const createMutation = useMutation({
    mutationFn: (formData: FormData) =>
      apiClient
        .post<ApiResponse<AuctionResponse>>("/api/auctions", formData, {
          headers: { "Content-Type": "multipart/form-data" },
        })
        .then((r) => r.data),
    onSuccess: (data) => {
      router.push(`/auctions/${data.data.id}`);
    },
    onError: (err: unknown) => {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setError(axiosError?.response?.data?.message ?? "경매 등록에 실패했습니다.");
    },
  });

  if (!isLoggedIn) {
    router.push("/login");
    return null;
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const request = {
      categoryId: Number(form.categoryId),
      title: form.title,
      description: form.description,
      startPrice: Number(form.startPrice),
      buyNowPrice: form.buyNowPrice ? Number(form.buyNowPrice) : null,
      minBidUnit: Number(form.minBidUnit),
      startAt: form.startAt,
      endAt: form.endAt,
    };

    const formData = new FormData();
    formData.append("request", new Blob([JSON.stringify(request)], { type: "application/json" }));
    images.forEach((img) => formData.append("images", img));

    createMutation.mutate(formData);
  };

  const categories = categoriesData?.data ?? [];

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">경매 등록</h1>

      <div className="card p-6">
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* 카테고리 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              카테고리 <span className="text-red-500">*</span>
            </label>
            <select
              className="input-field"
              value={form.categoryId}
              onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
              required
            >
              <option value="">카테고리 선택</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          {/* 상품명 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              상품명 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              className="input-field"
              placeholder="상품명을 입력하세요"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              required
              maxLength={100}
            />
          </div>

          {/* 상품 설명 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">상품 설명</label>
            <textarea
              className="input-field resize-none"
              rows={4}
              placeholder="상품을 자세히 설명해주세요"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>

          {/* 가격 설정 */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                시작가 <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                className="input-field"
                placeholder="원"
                value={form.startPrice}
                onChange={(e) => setForm({ ...form, startPrice: e.target.value })}
                required
                min={1}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                즉시 구매가
              </label>
              <input
                type="number"
                className="input-field"
                placeholder="원 (선택)"
                value={form.buyNowPrice}
                onChange={(e) => setForm({ ...form, buyNowPrice: e.target.value })}
                min={1}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                최소 입찰 단위 <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                className="input-field"
                value={form.minBidUnit}
                onChange={(e) => setForm({ ...form, minBidUnit: e.target.value })}
                required
                min={100}
              />
            </div>
          </div>

          {/* 경매 기간 */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                시작 일시 <span className="text-red-500">*</span>
              </label>
              <input
                type="datetime-local"
                className="input-field"
                value={form.startAt}
                onChange={(e) => setForm({ ...form, startAt: e.target.value })}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                종료 일시 <span className="text-red-500">*</span>
              </label>
              <input
                type="datetime-local"
                className="input-field"
                value={form.endAt}
                onChange={(e) => setForm({ ...form, endAt: e.target.value })}
                required
              />
            </div>
          </div>

          {/* 이미지 업로드 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              상품 이미지 (최대 5개, 첫 번째 이미지가 썸네일)
            </label>
            <input
              type="file"
              multiple
              accept="image/*"
              className="input-field"
              onChange={(e) => {
                const files = Array.from(e.target.files ?? []).slice(0, 5);
                setImages(files);
              }}
            />
            {images.length > 0 && (
              <p className="text-xs text-gray-500 mt-1">{images.length}개 선택됨</p>
            )}
          </div>

          {error && <p className="text-red-500 text-sm">{error}</p>}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              className="btn-secondary flex-1"
              onClick={() => router.back()}
            >
              취소
            </button>
            <button
              type="submit"
              className="btn-primary flex-1"
              disabled={createMutation.isPending}
            >
              {createMutation.isPending ? <LoadingSpinner size="sm" /> : "경매 등록"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
