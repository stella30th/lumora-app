"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Layers,
  GraduationCap,
  BarChart3,
  Settings,
  LogOut,
  Lightbulb,
} from "lucide-react";
import { getUser, clearAuth } from "@/lib/auth";
import { UserSession } from "@/types/user";

const navItems = [
  { label: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { label: "Decks", href: "/decks", icon: Layers },
  { label: "Study", href: "/study", icon: GraduationCap },
  { label: "Progress", href: "/progress", icon: BarChart3 },
  { label: "Settings", href: "/settings", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();
  const [user, setUser] = useState<UserSession | null>(null);

  useEffect(() => {
    setUser(getUser());
  }, []);

  const handleLogout = () => {
    clearAuth();
    window.location.href = "/login";
  };

  const getInitials = (name?: string) => {
    if (!name) return "U";
    return name.slice(0, 2).toUpperCase();
  };

  return (
    <aside className="w-60 shrink-0 border-r border-lumora-border bg-lumora-bg flex flex-col justify-between h-screen sticky top-0">
      {/* Top section: Logo & Nav */}
      <div className="p-5 flex flex-col gap-6">
        {/* Logo */}
        <Link href="/dashboard" className="flex items-center gap-3 px-2">
          <div className="w-[26px] h-[26px] rounded-[7px] bg-lumora-primary flex items-center justify-center text-lumora-bg">
            <Lightbulb className="w-[15px] h-[15px] stroke-[2.2]" />
          </div>
          <span className="font-bold text-[17px] tracking-tight text-lumora-primary">
            Lumora
          </span>
        </Link>

        {/* Nav Links */}
        <nav className="flex flex-col gap-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive =
              pathname === item.href ||
              (item.href !== "/dashboard" && pathname?.startsWith(item.href));

            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-btn text-body-default transition-colors ${
                  isActive
                    ? "bg-lumora-surface text-lumora-primary font-semibold"
                    : "text-lumora-secondary hover:text-lumora-primary hover:bg-lumora-surface-hover font-medium"
                }`}
              >
                <Icon className="w-[18px] h-[18px] stroke-[1.8] shrink-0" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      {/* Bottom section: Mini Profile & Logout */}
      <div className="p-4 border-t border-lumora-border flex items-center justify-between gap-2">
        <div className="flex items-center gap-2.5 min-w-0">
          <div className="w-8 h-8 rounded-full bg-lumora-surface border border-lumora-border flex items-center justify-center font-bold text-[11px] text-lumora-primary shrink-0">
            {getInitials(user?.username)}
          </div>
          <div className="min-w-0 flex flex-col">
            <span className="text-[13px] font-semibold text-lumora-primary truncate">
              {user?.username || "Learner"}
            </span>
            <span className="text-caption-xs text-lumora-muted truncate">
              {user?.email || "user@lumora.app"}
            </span>
          </div>
        </div>

        <button
          type="button"
          onClick={handleLogout}
          aria-label="Log out"
          title="Log out"
          className="p-1.5 rounded-btn text-lumora-secondary hover:text-lumora-danger hover:bg-lumora-surface-hover transition-colors shrink-0"
        >
          <LogOut className="w-4 h-4 stroke-[1.8]" />
        </button>
      </div>
    </aside>
  );
}
