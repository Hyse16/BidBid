"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import apiClient from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { UserResponse, UserUpdateRequest } from "@/types/user";
import { useAuthStore } from "@/store/authStore";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { formatDateTime } from "@/lib/format";

/**
 * 마이페이지 — 프로필 조회/수정
 */
export default function MyProfilePage() {
  const queryClient = useQueryClient();
  const { setUser } = useAuthStore();
  const [isEditing, setIsEditing] = useState(false);
  const [form, setForm] = useState<UserUpdateRequest>({ nickname: "", telegramChatId: null });
  const [updateError, setUpdateError] = useState("");

  // 내 프로필 조회
  const { data, isLoading } = useQuery<ApiResponse<UserResponse>>({
    queryKey: ["me"],
    queryFn: () => apiClient.get("/api/users/me").then((r) => r.data),
  });

  // 프로필 수정 뮤테이션
  const updateMutation = useMutation({
    mutationFn: (request: UserUpdateRequest) =>
      apiClient.put<ApiResponse<UserResponse>>("/api/users/me", request).then((r) => r.data),
    onSuccess: (data) => {
      setUser(data.data);
      queryClient.invalidateQueries({ queryKey: ["me"] });
      setIsEditing(false);
      setUpdateError("");
    },
    onError: (err: unknown) => {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setUpdateError(axiosError?.response?.data?.message ?? "수정에 실패했습니다.");
    },
  });

  if (isLoading) {
    return (
      <div className="py-20">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  const user = data?.data;
  if (!user) return null;

  const handleEditStart = () => {
    setForm({ nickname: user.nickname, telegramChatId: user.telegramChatId });
    setIsEditing(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(form);
  };

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">프로필</h1>

      <div className="card p-6 max-w-lg">
        {!isEditing ? (
          <div className="space-y-4">
            <ProfileField label="이메일" value={user.email} />
            <ProfileField label="닉네임" value={user.nickname} />
            <ProfileField label="권한" value={user.role === "ADMIN" ? "관리자" : "일반 사용자"} />
            <ProfileField
              label="텔레그램 Chat ID"
              value={user.telegramChatId ?? "미등록"}
            />
            <ProfileField label="가입일" value={formatDateTime(user.createdAt)} />

            <button onClick={handleEditStart} className="btn-primary mt-4">
              프로필 수정
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">닉네임</label>
              <input
                type="text"
                className="input-field"
                value={form.nickname}
                onChange={(e) => setForm({ ...form, nickname: e.target.value })}
                required
                minLength={2}
                maxLength={50}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                텔레그램 Chat ID
              </label>
              <input
                type="text"
                className="input-field"
                placeholder="텔레그램 알림을 받으려면 Chat ID를 입력하세요"
                value={form.telegramChatId ?? ""}
                onChange={(e) => setForm({ ...form, telegramChatId: e.target.value || null })}
              />
              <p className="text-xs text-gray-400 mt-1">
                텔레그램 @userinfobot에 /start를 보내면 Chat ID를 확인할 수 있습니다
              </p>
            </div>

            {updateError && <p className="text-red-500 text-sm">{updateError}</p>}

            <div className="flex gap-3">
              <button
                type="button"
                className="btn-secondary flex-1"
                onClick={() => setIsEditing(false)}
              >
                취소
              </button>
              <button
                type="submit"
                className="btn-primary flex-1"
                disabled={updateMutation.isPending}
              >
                {updateMutation.isPending ? <LoadingSpinner size="sm" /> : "저장"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

function ProfileField({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs font-medium text-gray-500 uppercase tracking-wide">{label}</p>
      <p className="text-sm text-gray-900 mt-0.5">{value}</p>
    </div>
  );
}
