"use client";

// Generic modal chrome: dark overlay + surface box + title + close button.
// DeckFormModal, CardFormModal and ConfirmDialog all render their own form/content
// as `children` inside this -- keeps the "how a modal looks/closes" logic in one
// place instead of copy-pasted into every modal (spec 2.3 asks for exactly this:
// overlay opacity, surface box, border, 12px radius, close by X or outside click).
import React, { useEffect } from "react";
import { X } from "lucide-react";

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  maxWidthClassName?: string;
}

export function Modal({
  open,
  onClose,
  title,
  children,
  maxWidthClassName = "max-w-[440px]",
}: ModalProps) {
  // Close on Escape while the modal is open.
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-lumora-bg/70 p-4"
      onMouseDown={(e) => {
        // Only close when the mousedown started on the overlay itself, not
        // when it started inside the box and the mouseup happened to land here.
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className={`w-full ${maxWidthClassName} bg-lumora-surface border border-lumora-border rounded-card p-6 shadow-sm`}
      >
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-card-heading font-semibold text-lumora-primary">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="text-lumora-secondary hover:text-lumora-primary transition-colors p-1 rounded"
          >
            <X className="w-4 h-4 stroke-[1.8]" />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
