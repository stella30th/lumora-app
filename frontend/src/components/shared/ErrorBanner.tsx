import React from "react";
import { AlertCircle } from "lucide-react";

interface ErrorBannerProps {
  message: string;
  className?: string;
}

export function ErrorBanner({ message, className = "" }: ErrorBannerProps) {
  if (!message) return null;

  return (
    <div
      role="alert"
      className={`flex items-center gap-2.5 p-3 rounded-input border border-lumora-danger/40 bg-lumora-danger/10 text-lumora-danger text-body-default font-medium transition-all ${className}`}
    >
      <AlertCircle className="w-4 h-4 shrink-0 stroke-[1.8]" />
      <span>{message}</span>
    </div>
  );
}
