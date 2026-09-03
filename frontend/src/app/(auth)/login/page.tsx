"use client";

import React, { useState, useEffect, Suspense } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import { apiClient } from "@/lib/api-client";
import { setAuth, isAuthenticated } from "@/lib/auth";
import { LoginResponse } from "@/types/user";
import { ErrorBanner } from "@/components/shared/ErrorBanner";

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    if (isAuthenticated()) {
      router.replace("/dashboard");
    }
    if (searchParams.get("expired") === "1") {
      setErrorMessage("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.");
    }
    if (searchParams.get("registered") === "1") {
      setSuccessMessage("Tạo tài khoản thành công! Đăng nhập để tiếp tục.");
    }
  }, [router, searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    if (!email.trim() || !password) {
      setErrorMessage("Vui lòng nhập đầy đủ email và mật khẩu.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await apiClient<LoginResponse>("/api/users/login", {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify({ email: email.trim(), password }),
      });

      setAuth({
        token: response.token,
        refreshToken: response.refreshToken,
        userId: response.userId,
        username: response.username,
        email: email.trim(),
      });

      router.push("/dashboard");
    } catch {
      // Per spec: Hiển thị đúng câu "Sai tên đăng nhập hoặc mật khẩu." không lộ chi tiết field nào sai
      setErrorMessage("Sai tên đăng nhập hoặc mật khẩu.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-lumora-bg p-4">
      <div className="w-full max-w-[400px] bg-lumora-surface border border-lumora-border rounded-card p-8 shadow-sm">
        {/* Logo */}
        <div className="flex items-center gap-2.5 mb-6">
          <div className="w-[26px] h-[26px] rounded-[7px] bg-lumora-primary flex items-center justify-center text-lumora-bg font-bold text-[14px]">
            L
          </div>
          <span className="font-bold text-[18px] tracking-tight text-lumora-primary">
            Lumora
          </span>
        </div>

        {/* Heading */}
        <h1 className="text-title-page font-bold text-lumora-primary mb-6">
          Đăng nhập
        </h1>

        {/* Success Alert */}
        {successMessage && (
          <div className="mb-4 p-3 rounded-input border border-lumora-border bg-lumora-surface-hover text-lumora-primary text-body-default font-medium">
            {successMessage}
          </div>
        )}

        {/* Error Alert using Danger token */}
        {errorMessage && (
          <div className="mb-4">
            <ErrorBanner message={errorMessage} />
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label
              htmlFor="email"
              className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
            >
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              required
              placeholder="ten@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
            >
              Mật khẩu
            </label>
            <div className="relative">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                autoComplete="current-password"
                required
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 pr-10 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-lumora-secondary hover:text-lumora-primary transition-colors"
              >
                {showPassword ? (
                  <EyeOff className="w-4 h-4 stroke-[1.8]" />
                ) : (
                  <Eye className="w-4 h-4 stroke-[1.8]" />
                )}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full mt-2 py-2.5 px-4 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-opacity"
          >
            {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
            <span>Đăng nhập</span>
          </button>
        </form>

        {/* Footer */}
        <p className="mt-6 text-center text-body-default text-lumora-secondary">
          Chưa có tài khoản?{" "}
          <Link
            href="/register"
            className="font-semibold text-lumora-primary hover:underline"
          >
            Đăng ký
          </Link>
        </p>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-lumora-bg" />}>
      <LoginForm />
    </Suspense>
  );
}
