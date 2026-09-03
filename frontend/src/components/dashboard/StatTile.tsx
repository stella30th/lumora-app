import React from "react";
import { LucideIcon } from "lucide-react";

interface StatTileProps {
  label: string;
  value: string | number;
  subtext?: string;
  icon?: LucideIcon;
}

export function StatTile({ label, value, subtext, icon: Icon }: StatTileProps) {
  return (
    <div className="bg-lumora-surface border border-lumora-border rounded-card p-5 flex flex-col justify-between transition-colors">
      <div className="flex items-center justify-between text-lumora-secondary">
        <span className="text-body-default font-medium">{label}</span>
        {Icon && <Icon className="w-4 h-4 stroke-[1.8]" />}
      </div>
      <div className="mt-3 flex items-baseline gap-2">
        <span className="text-title-lg font-bold text-lumora-primary font-mono tracking-tight">
          {value}
        </span>
        {subtext && (
          <span className="text-caption-xs text-lumora-muted">{subtext}</span>
        )}
      </div>
    </div>
  );
}
