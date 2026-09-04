"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  LayoutDashboard,
  Layers,
  GraduationCap,
  BarChart3,
  Settings,
  LogOut,
  Lightbulb,
} from "lucide-react";
import { getUser } from "@/lib/auth";
import { logoutAndClear } from "@/lib/api-client";
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
  const router = useRouter();
  const [user, setUser] = useState<UserSession | null>(null);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  useEffect(() => {
    setUser(getUser());
  }, []);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    await logoutAndClear();
    router.push("/login");
  };

  const getInitials = (name?: string) => {
    if (!name) return "U";
    return name.slice(0, 2).toUpperCase();
  };

  return (
    <aside className="w-16 lg:w-60 shrink-0 border-r border-lumora-border bg-lumora-bg flex flex-col justify-between h-screen sticky top-0">
      <div className="p-3 lg:p-5 flex flex-col gap-6">
        <Link href="/dashboard" className="flex items-center justify-center lg:justify-start gap-3 px-0 lg:px-2">
          <div className="w-[26px] h-[26px] rounded-[7px] bg-lumora-primary flex items-center justify-center text-lumora-bg shrink-0">
            <Lightbulb className="w-[15px] h-[15px] stroke-[2.2]" />
          </div>
          <span className="hidden lg:inline font-bold text-[17px] tracking-tight text-lumora-primary">
            Lumora
          </span>
        </Link>

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
                title={item.label}
                className={`flex items-center justify-center lg:justify-start gap-3 px-0 lg:px-3 py-2.5 rounded-btn text-body-default transition-colors ${
                  isActive
                    ? "bg-lumora-surface text-lumora-primary font-semibold"
                    : "text-lumora-secondary hover:text-lumora-primary hover:bg-lumora-surface-hover font-medium"
                }`}
              >
                <Icon className="w-[18px] h-[18px] stroke-[1.8] shrink-0" />
                <span className="hidden lg:inline">{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      <div className="p-3 lg:p-4 border-t border-lumora-border flex flex-col lg:flex-row items-center justify-between gap-2">
        <div className="flex items-center gap-2.5 min-w-0">
          <div className="w-8 h-8 rounded-full bg-lumora-surface border border-lumora-border flex items-center justify-center font-bold text-[11px] text-lumora-primary shrink-0">
            {getInitials(user?.username)}
          </div>
          <div className="hidden lg:flex min-w-0 flex-col">
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
          disabled={isLoggingOut}
          aria-label="Sign out"
          title="Sign out"
          className="p-1.5 rounded-btn text-lumora-secondary hover:text-lumora-danger hover:bg-lumora-surface-hover transition-colors shrink-0 disabled:opacity-50"
        >
          <LogOut className="w-4 h-4 stroke-[1.8]" />
        </button>
      </div>
    </aside>
  );
}
