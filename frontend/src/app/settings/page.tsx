"use client";

import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { LogOut, User, Palette } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { useTheme } from "@/components/providers/ThemeProvider";
import { getUser, isAuthenticated } from "@/lib/auth";
import { logoutAndClear } from "@/lib/api-client";
import { getUserById } from "@/lib/users";
import { UserResponse } from "@/types/user";

export default function SettingsPage() {
  const router = useRouter();
  const { theme } = useTheme();
  const localUser = getUser();

  useEffect(() => {
    if (!isAuthenticated()) router.replace("/login");
  }, [router]);

  const {
    data: user,
    isLoading,
    isError,
    error,
  } = useQuery<UserResponse, Error>({
    queryKey: ["users", localUser?.userId],
    queryFn: () => getUserById(localUser!.userId),
    enabled: !!localUser?.userId,
  });

  const handleLogout = async () => {
    await logoutAndClear();
    router.push("/login");
  };

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />
        <main className="p-8 max-w-2xl w-full mx-auto flex flex-col gap-8">
          <h1 className="text-title-page font-bold">Settings</h1>

          {isError && <ErrorBanner message={error?.message || "Could not load account info."} />}

          <section className="bg-lumora-surface border border-lumora-border rounded-card p-6 flex flex-col gap-4">
            <div className="flex items-center gap-2 text-lumora-secondary">
              <User className="w-4 h-4 stroke-[1.8]" />
              <h2 className="text-card-heading font-semibold text-lumora-primary">Account</h2>
            </div>

            {isLoading ? (
              <p className="text-body-default text-lumora-secondary">Loading…</p>
            ) : (
              <div className="flex flex-col gap-3">
                <div className="flex items-center justify-between gap-4">
                  <span className="text-body-default text-lumora-secondary">Username</span>
                  <span className="text-body-default font-medium text-lumora-primary">
                    {user?.username || localUser?.username || "—"}
                  </span>
                </div>
                <div className="flex items-center justify-between gap-4">
                  <span className="text-body-default text-lumora-secondary">Email</span>
                  <span className="text-body-default font-medium text-lumora-primary">
                    {user?.email || localUser?.email || "—"}
                  </span>
                </div>
                <div className="flex items-center justify-between gap-4">
                  <span className="text-body-default text-lumora-secondary">Member since</span>
                  <span className="text-body-default font-medium text-lumora-primary">
                    {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : "—"}
                  </span>
                </div>
              </div>
            )}
          </section>

          <section className="bg-lumora-surface border border-lumora-border rounded-card p-6 flex flex-col gap-4">
            <div className="flex items-center gap-2 text-lumora-secondary">
              <Palette className="w-4 h-4 stroke-[1.8]" />
              <h2 className="text-card-heading font-semibold text-lumora-primary">Appearance</h2>
            </div>
            <div className="flex items-center justify-between gap-4">
              <span className="text-body-default text-lumora-secondary">
                Theme — currently {theme === "dark" ? "Dark" : "Light"}
              </span>
              <ThemeToggle />
            </div>
          </section>

          <section className="bg-lumora-surface border border-lumora-border rounded-card p-6 flex flex-col gap-4">
            <h2 className="text-card-heading font-semibold text-lumora-primary">Session</h2>
            <button
              type="button"
              onClick={handleLogout}
              className="inline-flex items-center gap-2 self-start px-4 py-2 rounded-btn border border-lumora-border text-lumora-primary text-body-default font-semibold hover:bg-lumora-surface-hover transition-colors"
            >
              <LogOut className="w-4 h-4 stroke-[1.8]" />
              <span>Log Out</span>
            </button>
          </section>
        </main>
      </div>
    </div>
  );
}
