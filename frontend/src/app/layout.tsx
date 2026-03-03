import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import QueryProvider from "@/components/providers/QueryProvider";
import Header from "@/components/layout/Header";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "BidBid - 실시간 경매 플랫폼",
  description: "실시간 입찰로 원하는 상품을 득템하세요",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body className={inter.className}>
        <QueryProvider>
          <div className="min-h-screen flex flex-col bg-[#0f0f0f]">
            <Header />
            <main className="flex-1 container mx-auto px-4 py-8 max-w-6xl">
              {children}
            </main>
            <footer className="bg-[#0a0a0a] border-t border-[rgba(212,175,55,0.15)] py-6 text-center text-sm text-gray-600">
              © 2024{" "}
              <span className="text-[#d4af37] font-semibold tracking-widest">BidBid</span>
              {" "}— 실시간 경매 플랫폼
            </footer>
          </div>
        </QueryProvider>
      </body>
    </html>
  );
}
