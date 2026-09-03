"use client";

import React from "react";
import { Search, Bell } from "lucide-react";
import { ThemeToggle } from "./ThemeToggle";

interface TopbarProps {
  className?: string;
}

export function Topbar({ className = "" }: TopbarProps) {
  return (
    <header
      className={`h-16 border-b border-lumora-border bg-lumora-bg px-8 flex items-center justify-between sticky top-0 z-10 ${className}`}
    >
      {/* Search Input (Static for Day 1) */}
      <div className="relative w-72 md:w-80">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-lumora-secondary stroke-[1.8]" />
        <input
          type="text"
          placeholder="Search decks, vocabulary..."
          readOnly
          className="w-full bg-lumora-surface border border-lumora-border rounded-input pl-9 pr-4 py-2 text-body-default text-lumora-primary placeholder:text-lumora-secondary focus:outline-none cursor-default"
        />
      </div>

      {/* Right actions: Notifications & Theme Toggle */}
      <div className="flex items-center gap-3">
        <button
          type="button"
          aria-label="Notifications"
          title="Notifications (coming soon)"
          className="w-9 h-9 flex items-center justify-center rounded-btn border border-lumora-border bg-lumora-surface text-lumora-secondary hover:text-lumora-primary hover:bg-lumora-surface-hover transition-colors"
        >
          <Bell className="w-4 h-4 stroke-[1.8]" />
        </button>
        <ThemeToggle />
      </div>
    </header>
  );
}
