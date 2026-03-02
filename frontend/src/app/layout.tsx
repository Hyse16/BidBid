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
          <div className="min-h-screen flex flex-col bg-gray-50">
            <Header />
            <main className="flex-1 container mx-auto px-4 py-6 max-w-6xl">
              {children}
            </main>
            <footer className="bg-white border-t py-6 text-center text-sm text-gray-500">
              © 2024 BidBid. 실시간 경매 플랫폼
            </footer>
          </div>
        </QueryProvider>
      </body>
    </html>
  );
}
