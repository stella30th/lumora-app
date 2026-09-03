"use client";

import React from "react";
import { Moon, Sun } from "lucide-react";
import { useTheme } from "@/components/providers/ThemeProvider";

interface ThemeToggleProps {
  className?: string;
}

export function ThemeToggle({ className = "" }: ThemeToggleProps) {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      type="button"
      onClick={toggleTheme}
      aria-label="Chuyển chế độ sáng/tối"
      title={theme === "dark" ? "Chuyển sang giao diện sáng" : "Chuyển sang giao diện tối"}
      className={`w-9 h-9 flex items-center justify-center rounded-btn border border-lumora-border bg-lumora-surface text-lumora-secondary hover:text-lumora-primary hover:bg-lumora-surface-hover transition-colors ${className}`}
    >
      {theme === "dark" ? (
        <Sun className="w-4 h-4 stroke-[1.8]" />
      ) : (
        <Moon className="w-4 h-4 stroke-[1.8]" />
      )}
    </button>
  );
}
