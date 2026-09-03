"use client";

import React from "react";
import { Card } from "@/types/card";
import { DropdownMenu } from "@/components/shared/DropdownMenu";

interface CardRowProps {
  card: Card;
  onEdit: () => void;
  onDelete: () => void;
}

export function CardRow({ card, onEdit, onDelete }: CardRowProps) {
  return (
    <div className="flex items-center justify-between gap-4 px-4 py-3 border-b border-lumora-border last:border-b-0 hover:bg-lumora-surface-hover transition-colors">
      <div className="min-w-0 flex-1 grid grid-cols-2 gap-4">
        <p className="text-body-default text-lumora-primary font-medium truncate">{card.term}</p>
        <p className="text-body-default text-lumora-secondary truncate">{card.definition}</p>
      </div>
      <DropdownMenu
        ariaLabel="Card options"
        items={[
          { label: "Edit", onClick: onEdit },
          { label: "Delete", onClick: onDelete, danger: true },
        ]}
      />
    </div>
  );
}
