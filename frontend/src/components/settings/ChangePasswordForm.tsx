"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Eye, EyeOff, KeyRound, Loader2 } from "lucide-react";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { clearAuth } from "@/lib/auth";
import * as usersApi from "@/lib/users";

export function ChangePasswordForm() {
  const router = useRouter();

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPasswords, setShowPasswords] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!currentPassword || !newPassword || !confirmPassword) {
      setErrorMessage("Please fill in all fields.");
      return;
    }
    if (newPassword.length < 6) {
      setErrorMessage("New password must be at least 6 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setErrorMessage("New password and confirmation do not match.");
      return;
    }

    setIsSubmitting(true);
    try {
      await usersApi.changePassword({ currentPassword, newPassword });

      // Changing the password revokes every refresh token for this user
      // on the backend (see UserService#changePassword), so the refresh
      // token we're holding is already dead. The short-lived access token
      // would still work for a few minutes, but that's confusing UX — we
      // log out immediately and send the user back to log in with the
      // new password, same as any account-security flow should.
      clearAuth();
      router.push("/login?passwordChanged=1");
    } catch (err: unknown) {
      const error = err as Error;
      setErrorMessage(error.message || "Could not change your password.");
      setIsSubmitting(false);
    }
  };

  return (
    <section className="bg-lumora-surface border border-lumora-border rounded-card p-6 flex flex-col gap-4">
      <div className="flex items-center gap-2 text-lumora-secondary">
        <KeyRound className="w-4 h-4 stroke-[1.8]" />
        <h2 className="text-card-heading font-semibold text-lumora-primary">Change Password</h2>
      </div>

      {errorMessage && <ErrorBanner message={errorMessage} />}

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label
            htmlFor="current-password"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Current Password
          </label>
          <input
            id="current-password"
            type={showPasswords ? "text" : "password"}
            autoComplete="current-password"
            required
            placeholder="••••••••"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
          />
        </div>

        <div>
          <label
            htmlFor="new-password"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            New Password (min. 6 characters)
          </label>
          <div className="relative">
            <input
              id="new-password"
              type={showPasswords ? "text" : "password"}
              autoComplete="new-password"
              required
              minLength={6}
              placeholder="••••••••"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 pr-10 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
            />
            <button
              type="button"
              onClick={() => setShowPasswords(!showPasswords)}
              aria-label={showPasswords ? "Hide passwords" : "Show passwords"}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-lumora-secondary hover:text-lumora-primary transition-colors"
            >
              {showPasswords ? (
                <EyeOff className="w-4 h-4 stroke-[1.8]" />
              ) : (
                <Eye className="w-4 h-4 stroke-[1.8]" />
              )}
            </button>
          </div>
        </div>

        <div>
          <label
            htmlFor="confirm-new-password"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Confirm New Password
          </label>
          <input
            id="confirm-new-password"
            type={showPasswords ? "text" : "password"}
            autoComplete="new-password"
            required
            placeholder="••••••••"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
          />
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex items-center gap-2 self-start px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 disabled:opacity-50 transition-opacity"
        >
          {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
          <span>Update Password</span>
        </button>
      </form>
    </section>
  );
}
