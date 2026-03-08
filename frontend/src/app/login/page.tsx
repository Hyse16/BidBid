"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { useAuth } from "@/hooks/useAuth";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

function handleKakaoLogin() {
  window.location.href = `${API_URL}/oauth2/authorization/kakao`;
}
function handleGoogleLogin() {
  window.location.href = `${API_URL}/oauth2/authorization/google`;
}

// ── 아이콘 ──────────────────────────────────────────────────
function KakaoIcon() {
  return (
    <svg viewBox="0 0 24 24" className="w-5 h-5 flex-shrink-0" fill="currentColor">
      <path d="M12 3C6.477 3 2 6.477 2 10.8c0 2.72 1.57 5.117 3.95 6.594L4.8 21l4.41-2.32C10.18 18.88 11.08 19 12 19c5.523 0 10-3.477 10-8S17.523 3 12 3z" />
    </svg>
  );
}
function GoogleIcon() {
  return (
    <svg viewBox="0 0 24 24" className="w-5 h-5 flex-shrink-0">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05" />
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
  );
}

// ── 망치 아이콘 ─────────────────────────────────────────────
function GavelIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" className={className} fill="currentColor">
      <path d="M9.5 4.5L7 7 10 10l2.5-2.5L9.5 4.5zM6 8L3.5 10.5 7 14l2.5-2.5L6 8zM14 3l-3 3 3 3 3-3-3-3zM12 13l-8 8h4l8-8-4 0z" />
    </svg>
  );
}

// ── 실시간 입찰 데이터 ────────────────────────────────────────
const AUCTION_ITEMS = [
  { title: "롤렉스 서브마리너 2024", price: 19500000, user: "kim****", delta: "+500,000" },
  { title: "루이비통 네버풀 MM", price: 2150000, user: "par****", delta: "+50,000" },
  { title: "에르메스 버킨백 30cm", price: 8900000, user: "lee****", delta: "+200,000" },
  { title: "샤넬 클래식 플랩백", price: 7400000, user: "cho****", delta: "+150,000" },
  { title: "구찌 오피디아 GG백", price: 1320000, user: "yoo****", delta: "+30,000" },
  { title: "발렌시아가 트리플S 44", price: 890000, user: "han****", delta: "+20,000" },
  { title: "프라다 갈레리아 사피아노", price: 3200000, user: "oh****", delta: "+80,000" },
  { title: "티파니 솔리테어 다이아", price: 5600000, user: "jung****", delta: "+100,000" },
];

function useLiveBids() {
  const [bids, setBids] = useState<Array<{ id: number; title: string; price: number; user: string; delta: string; isNew: boolean }>>([]);
  const counter = useRef(0);

  useEffect(() => {
    const push = () => {
      const item = AUCTION_ITEMS[Math.floor(Math.random() * AUCTION_ITEMS.length)];
      const id = ++counter.current;
      setBids((prev) =>
        [{ ...item, price: item.price + Math.floor(Math.random() * 200000), id, isNew: true }, ...prev].slice(0, 5)
      );
      setTimeout(() => setBids((prev) => prev.map((b) => (b.id === id ? { ...b, isNew: false } : b))), 800);
    };
    push();
    const t = setInterval(push, 2500);
    return () => clearInterval(t);
  }, []);

  return bids;
}

function useCountdown(initialH = 1, initialM = 47, initialS = 33) {
  const [t, setT] = useState({ h: initialH, m: initialM, s: initialS });
  useEffect(() => {
    const id = setInterval(() => {
      setT((p) => {
        let { h, m, s } = p;
        if (--s < 0) { s = 59; if (--m < 0) { m = 59; if (--h < 0) h = 2; } }
        return { h, m, s };
      });
    }, 1000);
    return () => clearInterval(id);
  }, []);
  return t;
}

const pad = (n: number) => String(n).padStart(2, "0");

// ══════════════════════════════════════════════════════════
export default function LoginPage() {
  const { login, isLoginLoading, loginError } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [showPw, setShowPw] = useState(false);
  const [ready, setReady] = useState(false);
  const bids = useLiveBids();
  const time = useCountdown();

  useEffect(() => { setReady(true); }, []);

  const handleSubmit = (e: React.FormEvent) => { e.preventDefault(); login(form); };

  return (
    /*
     * fixed + z-[200]: Header(z-50)·Footer를 완전히 덮는 풀스크린 레이어
     * overflow-hidden: 내부 글로우가 화면 밖으로 흘러나오지 않도록
     */
    <div className="fixed inset-0 z-[200] flex overflow-hidden bg-[#06060e]">

      {/* ════════════════════════════════════
          LEFT — 경매장 분위기 패널
      ════════════════════════════════════ */}
      <div className="hidden lg:flex flex-col flex-1 relative overflow-hidden select-none">

        {/* 배경 레이어 */}
        <div className="absolute inset-0 bg-gradient-to-br from-[#0c0a1a] via-[#080816] to-[#06060e]" />

        {/* 글로우 오브 */}
        <div className="absolute -top-32 -left-32 w-[500px] h-[500px] rounded-full bg-[#d4af37]/10 blur-[140px] pointer-events-none" />
        <div className="absolute -bottom-32 right-0 w-[400px] h-[400px] rounded-full bg-[#8b5e0a]/8 blur-[120px] pointer-events-none" />

        {/* 격자 패턴 */}
        <div
          className="absolute inset-0 opacity-[0.035]"
          style={{
            backgroundImage:
              "linear-gradient(rgba(212,175,55,1) 1px,transparent 1px),linear-gradient(90deg,rgba(212,175,55,1) 1px,transparent 1px)",
            backgroundSize: "72px 72px",
          }}
        />

        <div className="relative z-10 flex flex-col h-full p-10">

          {/* 로고 */}
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#d4af37] to-[#9a7209] flex items-center justify-center shadow-[0_0_24px_rgba(212,175,55,0.5)]">
              <GavelIcon className="w-5 h-5 text-[#06060e]" />
            </div>
            <span className="text-2xl font-black tracking-[0.18em] text-[#d4af37] drop-shadow-[0_0_12px_rgba(212,175,55,0.4)]">
              BidBid
            </span>
          </div>

          {/* 헤드카피 */}
          <div className="flex-1 flex flex-col justify-center max-w-lg">
            <div
              className="inline-flex items-center gap-2 rounded-full border border-[#d4af37]/25 bg-[#d4af37]/8 px-4 py-1.5 mb-7 w-fit"
              style={ready ? { animation: "slide-up .5s ease both" } : {}}
            >
              <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
              <span className="text-[#d4af37] text-[11px] font-bold tracking-[.15em] uppercase">
                Live Auction
              </span>
            </div>

            <h2
              className="text-[3.2rem] font-black leading-[1.08] text-white mb-5"
              style={ready ? { animation: "slide-up .6s ease both" } : {}}
            >
              최고가를 향한<br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#d4af37] via-[#f5c530] to-[#c49a18]">
                실시간 경매
              </span>
            </h2>

            <p
              className="text-gray-400 text-[15px] leading-relaxed mb-10"
              style={ready ? { animation: "slide-up .7s ease both" } : {}}
            >
              프리미엄 품목을 실시간으로 경매하세요.<br />
              지금 이 순간에도 수백 개의 경매가 진행 중입니다.
            </p>

            {/* 마감 임박 경매 카드 */}
            <div
              className="rounded-2xl border border-[#d4af37]/20 bg-white/[0.025] p-5 mb-6 backdrop-blur-sm"
              style={ready ? { animation: "slide-up .8s ease both" } : {}}
            >
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
                  <span className="text-red-400 text-[11px] font-bold tracking-widest">ENDING SOON</span>
                </div>
                <div className="flex items-center gap-1 font-mono text-[#d4af37] text-sm font-bold tabular-nums bg-[#d4af37]/8 px-3 py-1 rounded-lg">
                  <svg className="w-3.5 h-3.5 mr-0.5 opacity-70" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6l4 2m6-2a10 10 0 11-20 0 10 10 0 0120 0z" />
                  </svg>
                  {pad(time.h)}:{pad(time.m)}:{pad(time.s)}
                </div>
              </div>
              <p className="text-white font-bold text-[15px] mb-1.5">롤렉스 데이토나 2023 블랙 다이얼</p>
              <div className="flex items-end gap-2">
                <span className="text-[#d4af37] text-[22px] font-black">19,500,000원</span>
                <span className="text-gray-500 text-xs mb-0.5 pb-px">현재 최고가</span>
              </div>
            </div>

            {/* 실시간 입찰 피드 */}
            <div style={ready ? { animation: "slide-up .9s ease both" } : {}}>
              <p className="text-[10px] text-gray-600 font-bold tracking-[.2em] uppercase mb-3">
                실시간 입찰 현황
              </p>
              <div className="space-y-1.5">
                {bids.map((bid) => (
                  <div
                    key={bid.id}
                    className="flex items-center justify-between rounded-xl px-4 py-2.5 transition-all duration-700"
                    style={{
                      background: bid.isNew
                        ? "linear-gradient(90deg,rgba(212,175,55,.14),rgba(212,175,55,.04))"
                        : "rgba(255,255,255,.018)",
                      borderLeft: `2px solid ${bid.isNew ? "#d4af37" : "rgba(212,175,55,.08)"}`,
                    }}
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-7 h-7 rounded-full bg-gradient-to-br from-[#d4af37]/30 to-[#9a7209]/20 flex items-center justify-center text-[10px] font-black text-[#d4af37] flex-shrink-0">
                        {bid.user[0].toUpperCase()}
                      </div>
                      <div className="min-w-0">
                        <p className="text-gray-300 text-xs font-semibold truncate">
                          {bid.title.length > 22 ? bid.title.slice(0, 22) + "…" : bid.title}
                        </p>
                        <p className="text-gray-600 text-[10px]">{bid.user}</p>
                      </div>
                    </div>
                    <div className="flex flex-col items-end flex-shrink-0 ml-3">
                      <span className={`text-sm font-black tabular-nums ${bid.isNew ? "text-[#d4af37]" : "text-gray-300"}`}>
                        {bid.price.toLocaleString()}원
                      </span>
                      <span className="text-[10px] text-emerald-500 font-semibold">{bid.delta}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 하단 통계 */}
          <div className="grid grid-cols-3 gap-3">
            {[
              { label: "진행 중 경매", value: "2,847", icon: "🔨" },
              { label: "오늘 낙찰액", value: "4.2억+", icon: "💰" },
              { label: "활성 입찰자", value: "18,394", icon: "👥" },
            ].map((s) => (
              <div key={s.label} className="rounded-xl border border-white/5 bg-white/[0.02] p-4 text-center">
                <div className="text-xl mb-1">{s.icon}</div>
                <div className="text-[#d4af37] font-black text-lg leading-none">{s.value}</div>
                <div className="text-gray-600 text-[10px] tracking-wide mt-1">{s.label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 수직 구분선 (lg 이상) */}
      <div className="hidden lg:block w-px bg-gradient-to-b from-transparent via-[#d4af37]/20 to-transparent flex-shrink-0" />

      {/* ════════════════════════════════════
          RIGHT — 로그인 폼
      ════════════════════════════════════ */}
      <div className="w-full lg:w-[460px] flex-shrink-0 flex flex-col relative overflow-y-auto">
        {/* 배경 */}
        <div className="absolute inset-0 bg-[#09091a]" />
        {/* 글로우 */}
        <div className="absolute top-0 right-0 w-[260px] h-[260px] rounded-full bg-[#d4af37]/6 blur-[100px] pointer-events-none" />
        <div className="absolute bottom-0 left-0 w-[200px] h-[200px] rounded-full bg-[#4a3800]/20 blur-[80px] pointer-events-none" />

        <div className="relative z-10 flex flex-col justify-center min-h-full px-10 py-12">

          {/* 모바일 로고 */}
          <div className="lg:hidden flex items-center gap-2.5 mb-10">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-[#d4af37] to-[#9a7209] flex items-center justify-center">
              <GavelIcon className="w-4 h-4 text-[#06060e]" />
            </div>
            <span className="text-xl font-black tracking-[.15em] text-[#d4af37]">BidBid</span>
          </div>

          {/* 타이틀 */}
          <div className="mb-8">
            <h1 className="text-[1.8rem] font-black text-white mb-1.5 leading-tight">
              경매장 입장
            </h1>
            <p className="text-gray-500 text-sm">
              로그인하고 실시간 경매에 참여하세요
            </p>
          </div>

          {/* SSO */}
          <div className="space-y-3 mb-7">
            <button
              onClick={handleKakaoLogin}
              type="button"
              className="w-full flex items-center justify-center gap-3 rounded-2xl bg-[#FEE500] py-3.5 px-5 text-[#191919] text-sm font-bold transition-all duration-200 hover:-translate-y-[2px] hover:shadow-[0_8px_24px_rgba(254,229,0,0.3)] active:translate-y-0"
            >
              <KakaoIcon />
              카카오로 시작하기
            </button>
            <button
              onClick={handleGoogleLogin}
              type="button"
              className="w-full flex items-center justify-center gap-3 rounded-2xl bg-white py-3.5 px-5 text-[#1f1f1f] text-sm font-bold transition-all duration-200 hover:-translate-y-[2px] hover:shadow-[0_8px_24px_rgba(255,255,255,0.12)] active:translate-y-0"
            >
              <GoogleIcon />
              Google로 시작하기
            </button>
          </div>

          {/* 구분선 */}
          <div className="flex items-center gap-4 mb-7">
            <div className="flex-1 h-px bg-gradient-to-r from-transparent to-white/8" />
            <span className="text-[11px] text-gray-600 tracking-widest font-medium whitespace-nowrap">
              이메일로 로그인
            </span>
            <div className="flex-1 h-px bg-gradient-to-l from-transparent to-white/8" />
          </div>

          {/* 폼 */}
          <form onSubmit={handleSubmit} className="space-y-4">

            {/* 이메일 */}
            <div>
              <label className="block text-[11px] font-bold text-gray-500 tracking-[.15em] uppercase mb-2">
                이메일
              </label>
              <div className="relative group">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-600 group-focus-within:text-[#d4af37] transition-colors duration-200 pointer-events-none">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
                  </svg>
                </span>
                <input
                  type="email"
                  className="w-full bg-white/[0.045] border border-white/[0.08] rounded-2xl pl-11 pr-4 py-3.5 text-sm text-gray-200 placeholder:text-gray-600 focus:outline-none focus:border-[#d4af37]/70 focus:bg-white/[0.07] focus:shadow-[0_0_0_3px_rgba(212,175,55,0.1)] transition-all duration-200"
                  placeholder="example@email.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  required
                />
              </div>
            </div>

            {/* 비밀번호 */}
            <div>
              <label className="block text-[11px] font-bold text-gray-500 tracking-[.15em] uppercase mb-2">
                비밀번호
              </label>
              <div className="relative group">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-600 group-focus-within:text-[#d4af37] transition-colors duration-200 pointer-events-none">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
                  </svg>
                </span>
                <input
                  type={showPw ? "text" : "password"}
                  className="w-full bg-white/[0.045] border border-white/[0.08] rounded-2xl pl-11 pr-12 py-3.5 text-sm text-gray-200 placeholder:text-gray-600 focus:outline-none focus:border-[#d4af37]/70 focus:bg-white/[0.07] focus:shadow-[0_0_0_3px_rgba(212,175,55,0.1)] transition-all duration-200"
                  placeholder="비밀번호 입력"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                />
                <button
                  type="button"
                  tabIndex={-1}
                  onClick={() => setShowPw(!showPw)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-600 hover:text-gray-300 transition-colors"
                >
                  {showPw ? (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                    </svg>
                  ) : (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {/* 에러 */}
            {loginError && (
              <div className="flex items-center gap-2.5 rounded-xl border border-red-500/20 bg-red-500/8 px-4 py-3">
                <svg className="w-4 h-4 text-red-400 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p className="text-red-400 text-xs font-medium">이메일 또는 비밀번호가 올바르지 않습니다.</p>
              </div>
            )}

            {/* 로그인 버튼 */}
            <button
              type="submit"
              disabled={isLoginLoading}
              className="w-full mt-2 flex items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-[#c9a227] via-[#e8c13a] to-[#b08a1a] py-4 text-sm font-black text-[#06060e] tracking-wide shadow-[0_4px_24px_rgba(212,175,55,0.28)] transition-all duration-200 hover:-translate-y-[2px] hover:shadow-[0_8px_32px_rgba(212,175,55,0.45)] active:translate-y-0 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoginLoading ? (
                <LoadingSpinner size="sm" />
              ) : (
                <>
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75" />
                  </svg>
                  경매장 입장하기
                </>
              )}
            </button>
          </form>

          {/* 회원가입 */}
          <p className="text-center text-sm text-gray-600 mt-7">
            아직 계정이 없으신가요?{" "}
            <Link href="/signup" className="text-[#d4af37] hover:text-[#f5c530] font-bold transition-colors">
              무료 가입 →
            </Link>
          </p>

          {/* 약관 */}
          <p className="text-center text-[10px] text-gray-700 mt-6 leading-relaxed">
            로그인 시{" "}
            <span className="text-gray-600 cursor-pointer hover:text-gray-400 transition-colors">이용약관</span>
            {" "}및{" "}
            <span className="text-gray-600 cursor-pointer hover:text-gray-400 transition-colors">개인정보처리방침</span>
            에 동의합니다.
          </p>
        </div>
      </div>
    </div>
  );
}
