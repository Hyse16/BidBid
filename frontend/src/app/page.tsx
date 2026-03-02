import { redirect } from "next/navigation";

/**
 * 루트 페이지 — 경매 목록으로 리다이렉트
 */
export default function Home() {
  redirect("/auctions");
}
