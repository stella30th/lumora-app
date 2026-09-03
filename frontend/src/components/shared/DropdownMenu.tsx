"use client";

// Small "kebab" dropdown menu (the vertical ⋮ button that opens Edit/Delete).
// Shared between DeckCard (deck options) and CardRow (card options) so the
// open/close-on-outside-click/Escape logic exists in exactly one place.
import React, { useEffect, useRef, useState } from "react";
import { MoreVertical } from "lucide-react";

export interface DropdownMenuItem {
  label: string;
  onClick: () => void;
  danger?: boolean;
}

interface DropdownMenuProps {
  items: DropdownMenuItem[];
  ariaLabel: string;
  className?: string;
}

export function DropdownMenu({ items, ariaLabel, className = "" }: DropdownMenuProps) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;

    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") setOpen(false);
    };

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEscape);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [open]);

  return (
    <div ref={containerRef} className={`relative ${className}`}>
      <button
        type="button"
        aria-label={ariaLabel}
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          setOpen((prev) => !prev);
        }}
        className="text-lumora-muted hover:text-lumora-primary p-1 rounded transition-colors"
      >
        <MoreVertical className="w-4 h-4 stroke-[1.8]" />
      </button>

      {open && (
        <div
          role="menu"
          className="absolute right-0 top-full mt-1 min-w-[140px] bg-lumora-surface border border-lumora-border rounded-btn shadow-sm py-1 z-20"
        >
          {items.map((item) => (
            <button
              key={item.label}
              type="button"
              role="menuitem"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                setOpen(false);
                item.onClick();
              }}
              className={`w-full text-left px-3 py-2 text-body-default transition-colors hover:bg-lumora-surface-hover ${
                item.danger ? "text-lumora-danger" : "text-lumora-primary"
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
