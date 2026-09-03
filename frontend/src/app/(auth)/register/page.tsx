"use client";

import React, { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import { apiClient } from "@/lib/api-client";
import { setAuth } from "@/lib/auth";
import { LoginResponse, UserResponse } from "@/types/user";
import { ErrorBanner } from "@/components/shared/ErrorBanner";

export default function RegisterPage() {
  const router = useRouter();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    // Client-side validation matching backend UserRegisterRequest DTO
    if (!username.trim() || !email.trim() || !password) {
      setErrorMessage("Vui lòng điền đầy đủ các trường thông tin.");
      return;
    }

    if (username.trim().length > 50) {
      setErrorMessage("Tên đăng nhập không được vượt quá 50 ký tự.");
      return;
    }

    if (password.length < 6) {
      setErrorMessage("Mật khẩu phải có tối thiểu 6 ký tự.");
      return;
    }

    if (password !== confirmPassword) {
      setErrorMessage("Mật khẩu xác nhận không khớp.");
      return;
    }

    setIsLoading(true);

    try {
      // 1. Call Register API
      await apiClient<UserResponse>("/api/users/register", {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify({
          username: username.trim(),
          email: email.trim(),
          password,
        }),
      });

      // 2. Tự động đăng nhập sau khi đăng ký thành công (Auto-login flow)
      // Giúp người dùng vào thẳng Dashboard mà không phải gõ lại thông tin
      try {
        const loginData = await apiClient<LoginResponse>("/api/users/login", {
          method: "POST",
          skipAuth: true,
          body: JSON.stringify({
            email: email.trim(),
            password,
          }),
        });

        setAuth({
          token: loginData.token,
          refreshToken: loginData.refreshToken,
          userId: loginData.userId,
          username: loginData.username,
          email: email.trim(),
        });

        router.push("/dashboard");
      } catch {
        // Fallback: nếu auto-login gặp trục trặc, đưa về trang login kèm thông báo
        router.push("/login?registered=1");
      }
    } catch (err: unknown) {
      const error = err as Error;
      setErrorMessage(
        error.message || "Đăng ký không thành công. Vui lòng thử lại."
      );
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
          Tạo tài khoản
        </h1>

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
              htmlFor="username"
              className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
            >
              Tên đăng nhập
            </label>
            <input
              id="username"
              type="text"
              required
              maxLength={50}
              placeholder="nguyenvana"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
            />
          </div>

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
              Mật khẩu (tối thiểu 6 ký tự)
            </label>
            <div className="relative">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                required
                minLength={6}
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

          <div>
            <label
              htmlFor="confirmPassword"
              className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
            >
              Xác nhận mật khẩu
            </label>
            <input
              id="confirmPassword"
              type={showPassword ? "text" : "password"}
              required
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full mt-2 py-2.5 px-4 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-opacity"
          >
            {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
            <span>Tạo tài khoản</span>
          </button>
        </form>

        {/* Footer */}
        <p className="mt-6 text-center text-body-default text-lumora-secondary">
          Đã có tài khoản?{" "}
          <Link
            href="/login"
            className="font-semibold text-lumora-primary hover:underline"
          >
            Đăng nhập
          </Link>
        </p>
      </div>
    </div>
  );
}
