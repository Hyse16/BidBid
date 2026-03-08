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
    <svg viewBox="0 0 24 24" className="w-5 h-5" fill="currentColor">
      <path d="M12 3C6.477 3 2 6.477 2 10.8c0 2.72 1.57 5.117 3.95 6.594L4.8 21l4.41-2.32C10.18 18.88 11.08 19 12 19c5.523 0 10-3.477 10-8S17.523 3 12 3z" />
    </svg>
  );
}
function GoogleIcon() {
  return (
    <svg viewBox="0 0 24 24" className="w-5 h-5">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05" />
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
  );
}

// ── 가짜 실시간 입찰 피드 ─────────────────────────────────────
const FAKE_ITEMS = [
  { title: "로렉스 서브마리너 2024", price: 12800000, user: "kim****" },
  { title: "루이비통 네버풀 MM", price: 2150000, user: "par****" },
  { title: "에르메스 버킨백 30", price: 8900000, user: "lee****" },
  { title: "샤넬 클래식 플랩백", price: 7400000, user: "cho****" },
  { title: "구찌 오피디아 GG백", price: 1320000, user: "yoo****" },
  { title: "롤렉스 데이토나 2023", price: 19500000, user: "han****" },
  { title: "발렌시아가 트리플S", price: 890000, user: "oh****" },
  { title: "프라다 갈레리아 사피아노", price: 3200000, user: "jung****" },
];

function useLiveBids() {
  const [bids, setBids] = useState<Array<{ id: number; title: string; price: number; user: string; flash: boolean }>>([]);
  const counterRef = useRef(0);

  useEffect(() => {
    const addBid = () => {
      const item = FAKE_ITEMS[Math.floor(Math.random() * FAKE_ITEMS.length)];
      const variance = Math.floor(Math.random() * 500000 - 100000);
      counterRef.current += 1;
      const id = counterRef.current;
      setBids((prev) => [{ ...item, price: item.price + variance, id, flash: true }, ...prev].slice(0, 6));
      setTimeout(() => {
        setBids((prev) => prev.map((b) => (b.id === id ? { ...b, flash: false } : b)));
      }, 600);
    };
    addBid();
    const interval = setInterval(addBid, 2200);
    return () => clearInterval(interval);
  }, []);

  return bids;
}

// ── 카운트다운 타이머 ─────────────────────────────────────────
function useCountdown() {
  const [time, setTime] = useState({ h: 2, m: 34, s: 17 });
  useEffect(() => {
    const t = setInterval(() => {
      setTime((prev) => {
        let { h, m, s } = prev;
        s--;
        if (s < 0) { s = 59; m--; }
        if (m < 0) { m = 59; h--; }
        if (h < 0) { h = 2; m = 59; s = 59; }
        return { h, m, s };
      });
    }, 1000);
    return () => clearInterval(t);
  }, []);
  return time;
}

function pad(n: number) { return String(n).padStart(2, "0"); }

// ── 메인 ─────────────────────────────────────────────────────
export default function LoginPage() {
  const { login, isLoginLoading, loginError } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [showPassword, setShowPassword] = useState(false);
  const bids = useLiveBids();
  const time = useCountdown();
  const [mounted, setMounted] = useState(false);

  useEffect(() => { setMounted(true); }, []);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    login(form);
  };

  return (
    <div className="fixed inset-0 flex overflow-hidden bg-[#07070f]">

      {/* ══════════════════════════════════════
          왼쪽 — 경매장 분위기 패널
      ══════════════════════════════════════ */}
      <div className="hidden lg:flex flex-col flex-1 relative overflow-hidden">

        {/* 배경 그라디언트 레이어 */}
        <div className="absolute inset-0 bg-gradient-to-br from-[#0d0b1a] via-[#0a0a16] to-[#07070f]" />
        {/* 골드 글로우 */}
        <div className="absolute top-[-10%] left-[-10%] w-[70%] h-[70%] rounded-full bg-[#d4af37]/8 blur-[180px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[60%] h-[60%] rounded-full bg-[#b8860b]/6 blur-[160px]" />

        {/* 배경 그리드 패턴 */}
        <div
          className="absolute inset-0 opacity-[0.04]"
          style={{
            backgroundImage: "linear-gradient(rgba(212,175,55,0.6) 1px, transparent 1px), linear-gradient(90deg, rgba(212,175,55,0.6) 1px, transparent 1px)",
            backgroundSize: "60px 60px",
          }}
        />

        <div className="relative z-10 flex flex-col h-full p-10">

          {/* 로고 */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#d4af37] to-[#b8860b] flex items-center justify-center shadow-[0_0_20px_rgba(212,175,55,0.4)]">
              <svg viewBox="0 0 24 24" className="w-6 h-6 text-[#07070f]" fill="currentColor">
                <path d="M19.5 3.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v14a2 2 0 002 2h6.257A4.493 4.493 0 0019 22a4.5 4.5 0 004.5-4.5A4.493 4.493 0 0019 13.257V2l-1.5 1.5zM5 16V4h12v9.257A4.493 4.493 0 0013.257 17H5v-1zm14 4a2.5 2.5 0 110-5 2.5 2.5 0 010 5z" />
              </svg>
            </div>
            <span className="text-2xl font-black tracking-[0.15em] text-[#d4af37]">BidBid</span>
          </div>

          {/* 메인 헤드라인 */}
          <div className="flex-1 flex flex-col justify-center max-w-lg">
            <div
              className="inline-flex items-center gap-2 bg-[#d4af37]/10 border border-[#d4af37]/20 rounded-full px-4 py-1.5 mb-6 w-fit"
              style={{ animation: mounted ? "slide-up 0.6s ease both" : "none" }}
            >
              <span className="w-2 h-2 rounded-full bg-[#d4af37] animate-pulse" />
              <span className="text-[#d4af37] text-xs font-semibold tracking-widest uppercase">Live Auction</span>
            </div>

            <h2
              className="text-5xl font-black leading-[1.1] text-white mb-4"
              style={{ animation: mounted ? "slide-up 0.7s ease both" : "none" }}
            >
              최고가를 향한<br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#d4af37] via-[#f5c842] to-[#b8860b]">
                실시간 경매
              </span>
            </h2>

            <p
              className="text-gray-400 text-base leading-relaxed mb-10"
              style={{ animation: mounted ? "slide-up 0.8s ease both" : "none" }}
            >
              프리미엄 품목을 실시간으로 경매하세요.<br />
              지금 이 순간에도 수백 개의 경매가 진행 중입니다.
            </p>

            {/* 현재 진행 중인 최고가 경매 */}
            <div
              className="bg-[#0d0b1a] border border-[#d4af37]/20 rounded-2xl p-5 mb-6"
              style={{ animation: mounted ? "slide-up 0.9s ease both" : "none" }}
            >
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
                  <span className="text-xs text-red-400 font-semibold tracking-wider">ENDING SOON</span>
                </div>
                <div className="font-mono text-[#d4af37] text-sm font-bold tabular-nums">
                  {pad(time.h)}:{pad(time.m)}:{pad(time.s)}
                </div>
              </div>
              <p className="text-white font-bold text-base mb-1">롤렉스 데이토나 2023 블랙 다이얼</p>
              <div className="flex items-end gap-2">
                <span className="text-[#d4af37] text-2xl font-black">19,500,000원</span>
                <span className="text-gray-500 text-xs mb-0.5">현재 최고가</span>
              </div>
            </div>

            {/* 실시간 입찰 피드 */}
            <div style={{ animation: mounted ? "slide-up 1s ease both" : "none" }}>
              <p className="text-xs text-gray-600 font-semibold tracking-widest uppercase mb-3">실시간 입찰 현황</p>
              <div className="space-y-2">
                {bids.map((bid) => (
                  <div
                    key={bid.id}
                    className="flex items-center justify-between rounded-xl px-4 py-2.5 transition-all duration-500"
                    style={{
                      background: bid.flash
                        ? "linear-gradient(90deg, rgba(212,175,55,0.15), rgba(212,175,55,0.05))"
                        : "rgba(255,255,255,0.02)",
                      borderLeft: `2px solid ${bid.flash ? "#d4af37" : "rgba(212,175,55,0.1)"}`,
                    }}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-7 h-7 rounded-full bg-gradient-to-br from-[#d4af37]/30 to-[#b8860b]/20 flex items-center justify-center text-[10px] font-bold text-[#d4af37]">
                        {bid.user[0].toUpperCase()}
                      </div>
                      <div>
                        <p className="text-gray-300 text-xs font-medium">{bid.title.length > 20 ? bid.title.slice(0, 20) + "…" : bid.title}</p>
                        <p className="text-gray-600 text-[10px]">{bid.user}</p>
                      </div>
                    </div>
                    <span className={`text-sm font-bold tabular-nums ${bid.flash ? "text-[#d4af37]" : "text-gray-300"}`}>
                      {bid.price.toLocaleString()}원
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 하단 통계 */}
          <div className="grid grid-cols-3 gap-4">
            {[
              { label: "진행 중인 경매", value: "2,847", icon: "🔨" },
              { label: "오늘 낙찰 금액", value: "4.2억+", icon: "💰" },
              { label: "활성 입찰자", value: "18,394", icon: "👥" },
            ].map((stat) => (
              <div key={stat.label} className="bg-white/[0.02] border border-white/5 rounded-xl p-4 text-center">
                <div className="text-xl mb-1">{stat.icon}</div>
                <div className="text-[#d4af37] font-black text-lg">{stat.value}</div>
                <div className="text-gray-600 text-[10px] tracking-wide mt-0.5">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* ══════════════════════════════════════
          오른쪽 — 로그인 폼
      ══════════════════════════════════════ */}
      <div className="w-full lg:w-[480px] flex flex-col justify-center relative overflow-y-auto">
        {/* 배경 */}
        <div className="absolute inset-0 bg-[#0a0a14]" />
        {/* 모바일용 골드 글로우 */}
        <div className="absolute top-0 right-0 w-[300px] h-[300px] rounded-full bg-[#d4af37]/5 blur-[100px] pointer-events-none" />

        <div className="relative z-10 flex flex-col min-h-full justify-center px-8 py-12 lg:px-12">

          {/* 모바일 로고 */}
          <div className="lg:hidden flex items-center gap-2 mb-10">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#d4af37] to-[#b8860b] flex items-center justify-center">
              <svg viewBox="0 0 24 24" className="w-5 h-5 text-[#07070f]" fill="currentColor">
                <path d="M19.5 3.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v14a2 2 0 002 2h6.257A4.493 4.493 0 0019 22a4.5 4.5 0 004.5-4.5A4.493 4.493 0 0019 13.257V2l-1.5 1.5zM5 16V4h12v9.257A4.493 4.493 0 0013.257 17H5v-1zm14 4a2.5 2.5 0 110-5 2.5 2.5 0 010 5z" />
              </svg>
            </div>
            <span className="text-xl font-black tracking-widest text-[#d4af37]">BidBid</span>
          </div>

          {/* 헤더 */}
          <div className="mb-8">
            <h1 className="text-3xl font-black text-white mb-2">경매 참여하기</h1>
            <p className="text-gray-500 text-sm">계정에 로그인하고 실시간 경매에 참여하세요</p>
          </div>

          {/* SSO 버튼 */}
          <div className="space-y-3 mb-7">
            <button
              onClick={handleKakaoLogin}
              type="button"
              className="w-full flex items-center justify-center gap-3 bg-[#FEE500] hover:bg-[#f5da00] text-[#191919] font-bold py-3.5 px-4 rounded-2xl transition-all duration-200 shadow-[0_0_24px_rgba(254,229,0,0.2)] hover:shadow-[0_0_32px_rgba(254,229,0,0.35)] hover:-translate-y-0.5 active:translate-y-0"
            >
              <KakaoIcon />
              카카오로 시작하기
            </button>
            <button
              onClick={handleGoogleLogin}
              type="button"
              className="w-full flex items-center justify-center gap-3 bg-white hover:bg-gray-100 text-[#1f1f1f] font-bold py-3.5 px-4 rounded-2xl transition-all duration-200 shadow-[0_0_24px_rgba(255,255,255,0.07)] hover:shadow-[0_0_32px_rgba(255,255,255,0.12)] hover:-translate-y-0.5 active:translate-y-0"
            >
              <GoogleIcon />
              Google로 시작하기
            </button>
          </div>

          {/* 구분선 */}
          <div className="flex items-center gap-4 mb-7">
            <div className="flex-1 h-px bg-gradient-to-r from-transparent to-white/10" />
            <span className="text-[11px] text-gray-600 tracking-widest font-medium">이메일로 로그인</span>
            <div className="flex-1 h-px bg-gradient-to-l from-transparent to-white/10" />
          </div>

          {/* 이메일 폼 */}
          <form onSubmit={handleSubmit} className="space-y-4">

            {/* 이메일 */}
            <div className="group">
              <label className="block text-[11px] font-semibold text-gray-500 mb-2 tracking-widest uppercase">
                이메일
              </label>
              <div className="relative">
                <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-600 group-focus-within:text-[#d4af37] transition-colors">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
                  </svg>
                </div>
                <input
                  type="email"
                  className="w-full bg-white/[0.04] border border-white/10 rounded-2xl pl-11 pr-4 py-3.5 text-sm text-gray-200 placeholder:text-gray-600 focus:outline-none focus:border-[#d4af37]/60 focus:bg-white/[0.06] focus:shadow-[0_0_0_3px_rgba(212,175,55,0.1)] transition-all duration-200"
                  placeholder="example@email.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  required
                />
              </div>
            </div>

            {/* 비밀번호 */}
            <div className="group">
              <label className="block text-[11px] font-semibold text-gray-500 mb-2 tracking-widest uppercase">
                비밀번호
              </label>
              <div className="relative">
                <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-600 group-focus-within:text-[#d4af37] transition-colors">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
                  </svg>
                </div>
                <input
                  type={showPassword ? "text" : "password"}
                  className="w-full bg-white/[0.04] border border-white/10 rounded-2xl pl-11 pr-12 py-3.5 text-sm text-gray-200 placeholder:text-gray-600 focus:outline-none focus:border-[#d4af37]/60 focus:bg-white/[0.06] focus:shadow-[0_0_0_3px_rgba(212,175,55,0.1)] transition-all duration-200"
                  placeholder="비밀번호 입력"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-600 hover:text-gray-300 transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                    </svg>
                  ) : (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {/* 에러 */}
            {loginError && (
              <div className="flex items-center gap-2.5 bg-red-500/10 border border-red-500/20 rounded-xl px-4 py-3">
                <svg className="w-4 h-4 text-red-400 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p className="text-red-400 text-xs font-medium">이메일 또는 비밀번호가 올바르지 않습니다.</p>
              </div>
            )}

            {/* 로그인 버튼 */}
            <button
              type="submit"
              className="w-full relative overflow-hidden bg-gradient-to-r from-[#d4af37] via-[#f5c842] to-[#b8860b] text-[#07070f] font-black py-4 rounded-2xl text-sm tracking-wide shadow-[0_0_30px_rgba(212,175,55,0.3)] hover:shadow-[0_0_40px_rgba(212,175,55,0.5)] hover:-translate-y-0.5 active:translate-y-0 transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed mt-2"
              disabled={isLoginLoading}
            >
              {isLoginLoading ? (
                <LoadingSpinner size="sm" />
              ) : (
                <span className="flex items-center justify-center gap-2">
                  <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75" />
                  </svg>
                  경매장 입장하기
                </span>
              )}
            </button>
          </form>

          {/* 회원가입 */}
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              아직 계정이 없으신가요?{" "}
              <Link
                href="/signup"
                className="text-[#d4af37] hover:text-[#f5c842] font-bold transition-colors"
              >
                무료 가입 →
              </Link>
            </p>
          </div>

          {/* 하단 약관 */}
          <p className="text-center text-[10px] text-gray-700 mt-8 leading-relaxed">
            로그인 시{" "}
            <span className="text-gray-600 hover:text-gray-400 cursor-pointer transition-colors">이용약관</span>
            {" "}및{" "}
            <span className="text-gray-600 hover:text-gray-400 cursor-pointer transition-colors">개인정보처리방침</span>
            에 동의합니다.
          </p>
        </div>
      </div>
    </div>
  );
}
