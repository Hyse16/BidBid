"use client";

import { useState } from "react";
import Link from "next/link";
import { useAuth } from "@/hooks/useAuth";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

/**
 * 로그인 페이지
 */
export default function LoginPage() {
  const { login, isLoginLoading, loginError } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    login(form);
  };

  return (
    <div className="max-w-md mx-auto mt-12">
      <div className="card p-8">
        <h1 className="text-2xl font-bold text-center mb-8">로그인</h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이메일
            </label>
            <input
              type="email"
              className="input-field"
              placeholder="이메일 주소"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              비밀번호
            </label>
            <input
              type="password"
              className="input-field"
              placeholder="비밀번호"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </div>

          {loginError && (
            <p className="text-red-500 text-sm">
              이메일 또는 비밀번호가 올바르지 않습니다.
            </p>
          )}

          <button
            type="submit"
            className="btn-primary w-full"
            disabled={isLoginLoading}
          >
            {isLoginLoading ? <LoadingSpinner size="sm" /> : "로그인"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-600 mt-6">
          계정이 없으신가요?{" "}
          <Link href="/signup" className="text-blue-600 hover:underline font-medium">
            회원가입
          </Link>
        </p>
      </div>
    </div>
  );
}
