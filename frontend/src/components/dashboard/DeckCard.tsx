import React from "react";
import Link from "next/link";
import { Folder, MoreVertical } from "lucide-react";
import { DeckSummary } from "@/types/deck";

interface DeckCardProps {
  deck: DeckSummary;
}

export function DeckCard({ deck }: DeckCardProps) {
  return (
    <div className="group bg-lumora-surface border border-lumora-border rounded-card p-5 flex flex-col justify-between hover:bg-lumora-surface-hover transition-colors relative">
      <div>
        <div className="flex items-start justify-between gap-2 mb-3">
          <div className="w-8 h-8 rounded-btn bg-lumora-bg border border-lumora-border flex items-center justify-center text-lumora-secondary group-hover:text-lumora-primary transition-colors">
            <Folder className="w-4 h-4 stroke-[1.8]" />
          </div>
          <button
            type="button"
            aria-label="Deck options"
            className="text-lumora-muted hover:text-lumora-primary p-1 rounded transition-colors"
          >
            <MoreVertical className="w-4 h-4 stroke-[1.8]" />
          </button>
        </div>

        <Link
          href={`/decks/${deck.id}`}
          className="font-semibold text-card-heading text-lumora-primary hover:underline line-clamp-1"
        >
          {deck.title}
        </Link>
        {deck.description && (
          <p className="text-body-default text-lumora-secondary line-clamp-2 mt-1">
            {deck.description}
          </p>
        )}
      </div>

      <div className="mt-4 pt-3 border-t border-lumora-border/60 flex items-center justify-between text-caption-xs text-lumora-muted font-mono">
        <span>{deck.totalCards} cards</span>
        <span>{deck.updatedAt || "New"}</span>
      </div>
    </div>
  );
}
