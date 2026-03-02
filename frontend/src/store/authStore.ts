import { create } from "zustand";
import { UserResponse } from "@/types/user";
import { tokenStorage } from "@/lib/api";

interface AuthState {
  user: UserResponse | null;
  isLoggedIn: boolean;

  // 액션
  setUser: (user: UserResponse) => void;
  logout: () => void;
}

/**
 * 인증 상태 전역 스토어 (Zustand)
 *
 * 로그인 후 사용자 정보를 메모리에 보관하고, 로그아웃 시 토큰과 상태를 초기화한다.
 * 페이지 새로고침 시에는 로그인 상태가 초기화되므로, 앱 진입 시 /api/users/me 호출로 복원한다.
 */
export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isLoggedIn: false,

  setUser: (user) => set({ user, isLoggedIn: true }),

  logout: () => {
    tokenStorage.clear();
    set({ user: null, isLoggedIn: false });
  },
}));
