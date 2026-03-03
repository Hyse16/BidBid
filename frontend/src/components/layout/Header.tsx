"use client";

import Link from "next/link";
import { useAuthStore } from "@/store/authStore";
import { useAuth } from "@/hooks/useAuth";

/**
 * 전역 헤더 컴포넌트 — 프리미엄 다크 테마
 *
 * 로그인 상태에 따라 네비게이션 링크를 다르게 표시한다.
 *   - 비로그인: 로그인 / 회원가입 링크
 *   - 로그인: 내 경매, 마이페이지, 로그아웃 링크
 */
export default function Header() {
  const { isLoggedIn, user } = useAuthStore();
  const { logout } = useAuth();

  return (
    <header className="bg-[#12121f] border-b border-[rgba(212,175,55,0.25)] sticky top-0 z-50 backdrop-blur-sm">
      <div className="container mx-auto px-4 max-w-6xl">
        <nav className="flex items-center justify-between h-16">
          {/* 골드 그라디언트 로고 */}
          <Link
            href="/auctions"
            className="text-2xl font-bold tracking-widest
                       bg-gradient-to-r from-[#d4af37] to-[#f59e0b]
                       bg-clip-text text-transparent
                       hover:from-[#f59e0b] hover:to-[#d4af37]
                       transition-all duration-300"
          >
            BIDBID
          </Link>

          {/* 네비게이션 */}
          <div className="flex items-center gap-6">
            <Link
              href="/auctions"
              className="text-gray-400 hover:text-[#f59e0b] text-sm font-medium
                         transition-colors duration-200 tracking-wide"
            >
              경매 목록
            </Link>

            {isLoggedIn ? (
              <>
                <Link
                  href="/auctions/new"
                  className="text-gray-400 hover:text-[#f59e0b] text-sm font-medium
                             transition-colors duration-200 tracking-wide"
                >
                  경매 등록
                </Link>
                <Link
                  href="/me"
                  className="text-gray-400 hover:text-[#f59e0b] text-sm font-medium
                             transition-colors duration-200 tracking-wide"
                >
                  {user?.nickname}님
                </Link>
                <button
                  onClick={logout}
                  className="text-sm font-medium text-gray-600 hover:text-red-400
                             transition-colors duration-200"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link
                  href="/login"
                  className="text-gray-400 hover:text-[#f59e0b] text-sm font-medium
                             transition-colors duration-200 tracking-wide"
                >
                  로그인
                </Link>
                <Link
                  href="/signup"
                  className="btn-primary text-sm py-1.5 px-4"
                >
                  회원가입
                </Link>
              </>
            )}
          </div>
        </nav>
      </div>
    </header>
  );
}
