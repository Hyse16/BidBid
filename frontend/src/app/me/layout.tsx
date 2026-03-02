"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const NAV_ITEMS = [
  { href: "/me", label: "프로필" },
  { href: "/me/auctions", label: "내 경매" },
  { href: "/me/bids", label: "입찰 내역" },
  { href: "/me/watchlist", label: "관심 목록" },
];

/**
 * 마이페이지 공통 레이아웃
 *
 * 왼쪽 사이드 네비게이션과 오른쪽 콘텐츠 영역으로 구성된다.
 */
export default function MyPageLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="flex gap-6">
      {/* 사이드 네비게이션 */}
      <aside className="w-48 flex-shrink-0">
        <div className="card p-2">
          <nav className="space-y-1">
            {NAV_ITEMS.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`block px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  pathname === item.href
                    ? "bg-blue-50 text-blue-600"
                    : "text-gray-600 hover:bg-gray-50"
                }`}
              >
                {item.label}
              </Link>
            ))}
          </nav>
        </div>
      </aside>

      {/* 메인 콘텐츠 */}
      <div className="flex-1 min-w-0">{children}</div>
    </div>
  );
}
