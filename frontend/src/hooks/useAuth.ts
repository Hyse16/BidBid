"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import apiClient, { tokenStorage } from "@/lib/api";
import { useAuthStore } from "@/store/authStore";
import { ApiResponse } from "@/types/api";
import { AuthTokens, LoginRequest, SignupRequest, UserResponse } from "@/types/user";
import { useEffect } from "react";

/**
 * 인증 관련 훅
 *
 * 로그인, 회원가입, 로그아웃, 현재 사용자 정보 로딩 기능을 제공한다.
 * 페이지 새로고침 후에도 토큰이 남아있으면 /api/users/me를 호출하여 상태를 복원한다.
 */
export function useAuth() {
  const router = useRouter();
  const { user, isLoggedIn, setUser, logout: clearAuth } = useAuthStore();

  // 앱 마운트 시 토큰이 있으면 사용자 정보 복원
  const { data: meData } = useQuery<ApiResponse<UserResponse>>({
    queryKey: ["me"],
    queryFn: () => apiClient.get("/api/users/me").then((r) => r.data),
    enabled: !!tokenStorage.getAccess() && !isLoggedIn,
    retry: false,
  });

  useEffect(() => {
    if (meData?.data) {
      setUser(meData.data);
    }
  }, [meData, setUser]);

  // 로그인 뮤테이션
  const loginMutation = useMutation({
    mutationFn: (request: LoginRequest) =>
      apiClient
        .post<ApiResponse<AuthTokens>>("/api/auth/login", request)
        .then((r) => r.data),
    onSuccess: async (data) => {
      const { accessToken, refreshToken } = data.data;
      tokenStorage.setTokens(accessToken, refreshToken);

      // 사용자 정보 로드
      const meResponse = await apiClient.get<ApiResponse<UserResponse>>("/api/users/me");
      setUser(meResponse.data.data);

      router.push("/auctions");
    },
  });

  // 회원가입 뮤테이션
  const signupMutation = useMutation({
    mutationFn: (request: SignupRequest) =>
      apiClient
        .post<ApiResponse<UserResponse>>("/api/auth/signup", request)
        .then((r) => r.data),
    onSuccess: () => {
      router.push("/login");
    },
  });

  // 로그아웃
  const logout = async () => {
    const refreshToken = tokenStorage.getRefresh();
    if (refreshToken) {
      // 서버 측 Refresh Token 무효화 (실패해도 로컬 상태 초기화)
      await apiClient.post("/api/auth/logout", { refreshToken }).catch(() => {});
    }
    clearAuth();
    router.push("/login");
  };

  return {
    user,
    isLoggedIn,
    login: loginMutation.mutate,
    signup: signupMutation.mutate,
    logout,
    loginError: loginMutation.error,
    signupError: signupMutation.error,
    isLoginLoading: loginMutation.isPending,
    isSignupLoading: signupMutation.isPending,
  };
}
