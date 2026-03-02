"use client";

import Link from "next/link";
import { useAuthStore } from "@/store/authStore";
import { useAuth } from "@/hooks/useAuth";

/**
 * 전역 헤더 컴포넌트
 *
 * 로그인 상태에 따라 네비게이션 링크를 다르게 표시한다.
 *   - 비로그인: 로그인 / 회원가입 링크
 *   - 로그인: 내 경매, 마이페이지, 로그아웃 링크
 */
export default function Header() {
  const { isLoggedIn, user } = useAuthStore();
  const { logout } = useAuth();

  return (
    <header className="bg-white border-b shadow-sm sticky top-0 z-50">
      <div className="container mx-auto px-4 max-w-6xl">
        <nav className="flex items-center justify-between h-16">
          {/* 로고 */}
          <Link
            href="/auctions"
            className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition-colors"
          >
            BidBid
          </Link>

          {/* 네비게이션 */}
          <div className="flex items-center gap-4">
            <Link
              href="/auctions"
              className="text-gray-600 hover:text-blue-600 text-sm font-medium transition-colors"
            >
              경매 목록
            </Link>

            {isLoggedIn ? (
              <>
                <Link
                  href="/auctions/new"
                  className="text-gray-600 hover:text-blue-600 text-sm font-medium transition-colors"
                >
                  경매 등록
                </Link>
                <Link
                  href="/me"
                  className="text-gray-600 hover:text-blue-600 text-sm font-medium transition-colors"
                >
                  {user?.nickname}님
                </Link>
                <button
                  onClick={logout}
                  className="text-sm font-medium text-red-500 hover:text-red-600 transition-colors"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link
                  href="/login"
                  className="text-gray-600 hover:text-blue-600 text-sm font-medium transition-colors"
                >
                  로그인
                </Link>
                <Link href="/signup" className="btn-primary text-sm">
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
