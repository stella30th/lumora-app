import React from "react";
import { Folder, LucideIcon } from "lucide-react";

interface EmptyStateProps {
  icon?: LucideIcon;
  message: string;
  actionLabel?: string;
  onAction?: () => void;
  className?: string;
}

export function EmptyState({
  icon: Icon = Folder,
  message,
  actionLabel,
  onAction,
  className = "",
}: EmptyStateProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center text-center py-16 px-6 border border-dashed border-lumora-border rounded-card ${className}`}
    >
      <div className="w-12 h-12 rounded-btn bg-lumora-surface border border-lumora-border flex items-center justify-center text-lumora-muted mb-4">
        <Icon className="w-6 h-6 stroke-[1.6]" />
      </div>
      <p className="text-body-default text-lumora-secondary max-w-sm">{message}</p>
      {actionLabel && onAction && (
        <button
          type="button"
          onClick={onAction}
          className="mt-5 inline-flex items-center gap-2 px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 transition-opacity"
        >
          {actionLabel}
        </button>
      )}
    </div>
  );
}
