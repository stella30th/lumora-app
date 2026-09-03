import React from "react";

interface FlashcardProps {
  term: string;
  definition: string;
  flipped: boolean;
  onFlip: () => void;
}

// Stacked-deck look from the mockup: two faint blank layers peeking out behind
// the active card, front/back swap on click (or Space, wired in the page).
export function Flashcard({ term, definition, flipped, onFlip }: FlashcardProps) {
  return (
    <div className="relative w-full max-w-xl">
      <div className="absolute inset-x-6 -bottom-3 h-full rounded-card bg-lumora-surface border border-lumora-border opacity-40" />
      <div className="absolute inset-x-3 -bottom-1.5 h-full rounded-card bg-lumora-surface border border-lumora-border opacity-70" />

      <button
        type="button"
        onClick={onFlip}
        className="relative w-full min-h-[280px] bg-lumora-surface border border-lumora-border rounded-card p-8 flex flex-col items-center justify-center text-center gap-4"
      >
        <span className="text-caption-xs font-mono uppercase tracking-wider text-lumora-muted">
          {flipped ? "Back" : "Front"}
        </span>
        <p className="text-[34px] leading-tight font-bold text-lumora-primary break-words">
          {flipped ? definition : term}
        </p>
        {!flipped && (
          <span className="text-body-default text-lumora-secondary">
            Tap or press Space to reveal
          </span>
        )}
      </button>
    </div>
  );
}
