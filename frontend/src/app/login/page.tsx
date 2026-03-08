"use client";

import { useState } from "react";
import Link from "next/link";
import { useAuth } from "@/hooks/useAuth";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

// ── SSO 핸들러 (백엔드 OAuth 엔드포인트 연결 시 URL 교체) ──
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
      <path
        d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
        fill="#4285F4"
      />
      <path
        d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
        fill="#34A853"
      />
      <path
        d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"
        fill="#FBBC05"
      />
      <path
        d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
        fill="#EA4335"
      />
    </svg>
  );
}

// ── 메인 컴포넌트 ────────────────────────────────────────────
export default function LoginPage() {
  const { login, isLoginLoading, loginError } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    login(form);
  };

  return (
    <div className="min-h-[calc(100vh-140px)] flex items-center justify-center px-4">
      {/* 배경 글로우 */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full bg-[#d4af37]/5 blur-[120px]" />
      </div>

      <div className="w-full max-w-md relative z-10">
        {/* 로고 */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-black tracking-widest text-[#d4af37] drop-shadow-[0_0_20px_rgba(212,175,55,0.5)]">
            BidBid
          </h1>
          <p className="mt-2 text-gray-500 text-sm tracking-wide">
            실시간 프리미엄 경매 플랫폼
          </p>
        </div>

        {/* 카드 */}
        <div className="bg-[#141420] rounded-2xl border border-[rgba(212,175,55,0.2)] shadow-[0_8px_40px_rgba(0,0,0,0.6)] p-8">
          <h2 className="text-xl font-bold text-gray-100 mb-6 text-center">로그인</h2>

          {/* ── SSO 버튼 ── */}
          <div className="space-y-3 mb-6">
            {/* 카카오 */}
            <button
              onClick={handleKakaoLogin}
              type="button"
              className="w-full flex items-center justify-center gap-3 bg-[#FEE500] hover:bg-[#f0d800] text-[#191919] font-semibold py-3 px-4 rounded-xl transition-all duration-200 shadow-[0_2px_12px_rgba(254,229,0,0.25)] hover:shadow-[0_4px_20px_rgba(254,229,0,0.4)] hover:-translate-y-0.5"
            >
              <KakaoIcon />
              카카오로 계속하기
            </button>

            {/* 구글 */}
            <button
              onClick={handleGoogleLogin}
              type="button"
              className="w-full flex items-center justify-center gap-3 bg-white hover:bg-gray-50 text-[#1f1f1f] font-semibold py-3 px-4 rounded-xl transition-all duration-200 shadow-[0_2px_12px_rgba(255,255,255,0.1)] hover:shadow-[0_4px_20px_rgba(255,255,255,0.15)] hover:-translate-y-0.5"
            >
              <GoogleIcon />
              Google로 계속하기
            </button>
          </div>

          {/* ── 구분선 ── */}
          <div className="flex items-center gap-3 mb-6">
            <div className="flex-1 h-px bg-[rgba(212,175,55,0.15)]" />
            <span className="text-xs text-gray-600 tracking-widest">또는 이메일로</span>
            <div className="flex-1 h-px bg-[rgba(212,175,55,0.15)]" />
          </div>

          {/* ── 이메일 폼 ── */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-gray-400 mb-1.5 tracking-wide">
                이메일
              </label>
              <input
                type="email"
                className="input-field text-sm py-3"
                placeholder="example@email.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-400 mb-1.5 tracking-wide">
                비밀번호
              </label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  className="input-field text-sm py-3 pr-10"
                  placeholder="비밀번호 입력"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-300 transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  ) : (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {loginError && (
              <div className="flex items-center gap-2 bg-red-900/20 border border-red-800/40 rounded-lg px-3 py-2">
                <svg className="w-4 h-4 text-red-500 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p className="text-red-400 text-xs">이메일 또는 비밀번호가 올바르지 않습니다.</p>
              </div>
            )}

            <button
              type="submit"
              className="btn-primary w-full py-3 text-sm mt-2"
              disabled={isLoginLoading}
            >
              {isLoginLoading ? <LoadingSpinner size="sm" /> : "로그인"}
            </button>
          </form>

          {/* ── 회원가입 링크 ── */}
          <p className="text-center text-sm text-gray-600 mt-6">
            아직 계정이 없으신가요?{" "}
            <Link
              href="/signup"
              className="text-[#d4af37] hover:text-[#f59e0b] font-semibold transition-colors underline-offset-2 hover:underline"
            >
              무료 회원가입
            </Link>
          </p>
        </div>

        {/* 하단 설명 */}
        <p className="text-center text-xs text-gray-700 mt-6 leading-relaxed">
          로그인 시{" "}
          <span className="text-gray-600">이용약관</span> 및{" "}
          <span className="text-gray-600">개인정보처리방침</span>에 동의하는 것으로 간주됩니다.
        </p>
      </div>
    </div>
  );
}
