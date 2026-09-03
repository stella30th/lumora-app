"use client";

import React from "react";
import Link from "next/link";
import { Folder } from "lucide-react";
import { Deck } from "@/types/deck";
import { formatRelativeTime } from "@/lib/format";
import { DropdownMenu } from "@/components/shared/DropdownMenu";

interface DeckCardProps {
  deck: Deck;
  cardCount?: number;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function DeckCard({ deck, cardCount, onEdit, onDelete }: DeckCardProps) {
  return (
    <div className="group bg-lumora-surface border border-lumora-border rounded-card p-5 flex flex-col justify-between hover:bg-lumora-surface-hover transition-colors relative">
      <div>
        <div className="flex items-start justify-between gap-2 mb-3">
          <div className="w-8 h-8 rounded-btn bg-lumora-bg border border-lumora-border flex items-center justify-center text-lumora-secondary group-hover:text-lumora-primary transition-colors">
            <Folder className="w-4 h-4 stroke-[1.8]" />
          </div>
          {onEdit && onDelete && (
            <DropdownMenu
              ariaLabel="Deck options"
              items={[
                { label: "Edit", onClick: onEdit },
                { label: "Delete", onClick: onDelete, danger: true },
              ]}
            />
          )}
        </div>

        <Link
          href={`/decks/${deck.id}`}
          className="font-semibold text-card-heading text-lumora-primary hover:underline line-clamp-1"
        >
          {deck.name}
        </Link>
        {deck.description && (
          <p className="text-body-default text-lumora-secondary line-clamp-2 mt-1">
            {deck.description}
          </p>
        )}
      </div>

      <div className="mt-4 pt-3 border-t border-lumora-border/60 flex items-center justify-between text-caption-xs text-lumora-muted font-mono">
        <span>{cardCount === undefined ? "…" : `${cardCount} card${cardCount === 1 ? "" : "s"}`}</span>
        <span>{formatRelativeTime(deck.createdAt)}</span>
      </div>
    </div>
  );
}
