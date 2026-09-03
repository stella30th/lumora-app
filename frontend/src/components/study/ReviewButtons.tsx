import React from "react";

interface ReviewButtonsProps {
  onAnswer: (correct: boolean) => void;
  disabled?: boolean;
}

export function ReviewButtons({ onAnswer, disabled }: ReviewButtonsProps) {
  return (
    <div className="grid grid-cols-2 gap-3 w-full max-w-xl">
      <button
        type="button"
        disabled={disabled}
        onClick={() => onAnswer(false)}
        className="px-4 py-3 rounded-btn bg-lumora-surface border border-lumora-border text-lumora-primary font-semibold hover:bg-lumora-surface-hover transition-colors disabled:opacity-50"
      >
        Incorrect <span className="text-caption-xs text-lumora-muted font-normal">(1)</span>
      </button>
      <button
        type="button"
        disabled={disabled}
        onClick={() => onAnswer(true)}
        className="px-4 py-3 rounded-btn bg-lumora-surface border border-lumora-border text-lumora-primary font-semibold hover:bg-lumora-surface-hover transition-colors disabled:opacity-50"
      >
        Correct <span className="text-caption-xs text-lumora-muted font-normal">(2)</span>
      </button>
    </div>
  );
}
