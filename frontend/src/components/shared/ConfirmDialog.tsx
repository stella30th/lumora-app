"use client";

import React from "react";
import { Modal } from "./Modal";

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  description: string;
  confirmLabel?: string;
  cancelLabel?: string;
  danger?: boolean;
  isLoading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  danger = false,
  isLoading = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  return (
    <Modal open={open} onClose={onCancel} title={title} maxWidthClassName="max-w-[380px]">
      <p className="text-body-default text-lumora-secondary mb-6">{description}</p>
      <div className="flex items-center justify-end gap-3">
        <button
          type="button"
          onClick={onCancel}
          disabled={isLoading}
          className="px-4 py-2 rounded-btn border border-lumora-border bg-lumora-surface text-lumora-primary hover:bg-lumora-surface-hover transition-colors font-semibold text-body-default disabled:opacity-50"
        >
          {cancelLabel}
        </button>
        <button
          type="button"
          onClick={onConfirm}
          disabled={isLoading}
          className={`px-4 py-2 rounded-btn font-semibold text-body-default transition-opacity disabled:opacity-50 ${
            danger
              ? "bg-lumora-danger text-white hover:opacity-90"
              : "bg-lumora-btn text-lumora-btn-text hover:opacity-90"
          }`}
        >
          {isLoading ? "..." : confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
