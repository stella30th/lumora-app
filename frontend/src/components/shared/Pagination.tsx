"use client";

import React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";

interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

function getPageNumbers(page: number, totalPages: number): (number | "ellipsis")[] {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  const pages: (number | "ellipsis")[] = [0];

  if (page > 2) {
    pages.push("ellipsis");
  }

  const start = Math.max(1, page - 1);
  const end = Math.min(totalPages - 2, page + 1);
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  if (page < totalPages - 3) {
    pages.push("ellipsis");
  }

  pages.push(totalPages - 1);

  return pages;
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  const pageNumbers = getPageNumbers(page, totalPages);

  return (
    <nav aria-label="Pagination" className="flex items-center justify-center gap-1.5 pt-2">
      <button
        type="button"
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        aria-label="Previous page"
        className="w-9 h-9 flex items-center justify-center rounded-btn border border-lumora-border bg-lumora-surface text-lumora-primary hover:bg-lumora-surface-hover transition-colors disabled:opacity-40 disabled:hover:bg-lumora-surface disabled:cursor-not-allowed"
      >
        <ChevronLeft className="w-4 h-4 stroke-[1.8]" />
      </button>

      {pageNumbers.map((p, idx) =>
        p === "ellipsis" ? (
          <span
            key={`ellipsis-${idx}`}
            className="w-9 h-9 flex items-center justify-center text-lumora-muted text-body-default"
          >
            …
          </span>
        ) : (
          <button
            key={p}
            type="button"
            onClick={() => onPageChange(p)}
            aria-current={p === page ? "page" : undefined}
            className={`w-9 h-9 flex items-center justify-center rounded-btn text-body-default font-semibold transition-colors ${
              p === page
                ? "bg-lumora-btn text-lumora-btn-text"
                : "border border-lumora-border bg-lumora-surface text-lumora-primary hover:bg-lumora-surface-hover"
            }`}
          >
            {p + 1}
          </button>
        )
      )}

      <button
        type="button"
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        aria-label="Next page"
        className="w-9 h-9 flex items-center justify-center rounded-btn border border-lumora-border bg-lumora-surface text-lumora-primary hover:bg-lumora-surface-hover transition-colors disabled:opacity-40 disabled:hover:bg-lumora-surface disabled:cursor-not-allowed"
      >
        <ChevronRight className="w-4 h-4 stroke-[1.8]" />
      </button>
    </nav>
  );
}
