import React from "react";
import { ReviewRating } from "@/types/card";

interface ReviewButtonsProps {
  onAnswer: (rating: ReviewRating) => void;
  disabled?: boolean;
}

const OPTIONS: { rating: ReviewRating; label: string; hint: string; key: string }[] = [
  { rating: "AGAIN", label: "Again", hint: "Forgot it", key: "1" },
  { rating: "HARD", label: "Hard", hint: "Barely recalled", key: "2" },
  { rating: "GOOD", label: "Good", hint: "Recalled it", key: "3" },
  { rating: "EASY", label: "Easy", hint: "Instantly", key: "4" },
];

export function ReviewButtons({ onAnswer, disabled }: ReviewButtonsProps) {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 w-full max-w-2xl">
      {OPTIONS.map(({ rating, label, hint, key }) => (
        <button
          key={rating}
          type="button"
          disabled={disabled}
          onClick={() => onAnswer(rating)}
          className="px-4 py-3 rounded-btn bg-lumora-surface border border-lumora-border text-lumora-primary font-semibold hover:bg-lumora-surface-hover transition-colors disabled:opacity-50 flex flex-col items-center gap-0.5"
        >
          <span>
            {label} <span className="text-caption-xs text-lumora-muted font-normal">({key})</span>
          </span>
          <span className="text-caption-xs text-lumora-muted font-normal">{hint}</span>
        </button>
      ))}
    </div>
  );
}
