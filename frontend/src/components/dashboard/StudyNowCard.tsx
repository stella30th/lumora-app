import React from "react";
import Link from "next/link";
import { ArrowRight, Sparkles } from "lucide-react";

interface StudyNowCardProps {
  deckId?: number;
  deckTitle?: string;
  dueCards?: number;
}

export function StudyNowCard({
  deckId = 1,
  deckTitle = "Everyday English Vocabulary",
  dueCards = 12,
}: StudyNowCardProps) {
  return (
    <div className="bg-lumora-surface border border-lumora-border rounded-card p-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div className="flex items-start gap-4">
        <div className="w-10 h-10 rounded-btn bg-lumora-bg border border-lumora-border flex items-center justify-center text-lumora-primary shrink-0 mt-0.5">
          <Sparkles className="w-5 h-5 stroke-[1.8]" />
        </div>
        <div>
          <span className="text-caption-xs font-mono uppercase tracking-wider text-lumora-muted">
            Pick up where you left off
          </span>
          <h2 className="text-card-heading font-semibold text-lumora-primary mt-0.5">
            {deckTitle}
          </h2>
          <p className="text-body-default text-lumora-secondary mt-1">
            You have <span className="font-mono font-semibold text-lumora-primary">{dueCards}</span> cards
            to review today.
          </p>
        </div>
      </div>

      <Link
        href={`/study/${deckId}`}
        className="inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 transition-opacity shrink-0"
      >
        <span>Continue Studying</span>
        <ArrowRight className="w-4 h-4 stroke-[2]" />
      </Link>
    </div>
  );
}
