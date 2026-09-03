import React from "react";

interface ProgressBarProps {
  current: number;
  total: number;
}

export function ProgressBar({ current, total }: ProgressBarProps) {
  const pct = total > 0 ? Math.min(100, (current / total) * 100) : 0;

  return (
    <div className="flex items-center gap-3 w-full max-w-xs">
      <span className="text-caption-xs font-mono text-lumora-secondary shrink-0">
        {current} / {total}
      </span>
      <div className="flex-1 h-1 rounded-full bg-lumora-surface overflow-hidden">
        <div className="h-full bg-lumora-primary transition-all" style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}
