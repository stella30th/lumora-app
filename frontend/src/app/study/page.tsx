"use client";

import React, { useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { GraduationCap } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { isAuthenticated } from "@/lib/auth";
import * as decksApi from "@/lib/decks";
import { Deck } from "@/types/deck";

// Not in the original Day 3 spec (which only defines /study/[deckId]) -- added
// so the Sidebar's "Study" link has somewhere to go instead of staying a
// placeholder forever. See frontend/docs/day3-notes.md.
export default function StudyDeckPickerPage() {
  const router = useRouter();

  useEffect(() => {
    if (!isAuthenticated()) router.replace("/login");
  }, [router]);

  const {
    data: decks,
    isLoading,
    isError,
    error,
  } = useQuery<Deck[], Error>({
    queryKey: ["decks"],
    queryFn: decksApi.getDecks,
  });

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />
        <main className="p-8 max-w-7xl w-full mx-auto flex flex-col gap-6">
          <div>
            <h1 className="text-title-page font-bold">Study</h1>
            <p className="text-body-default text-lumora-secondary mt-1">
              Pick a deck to start reviewing.
            </p>
          </div>

          {isError && <ErrorBanner message={error?.message || "Could not load your decks."} />}
          {isLoading && <p className="text-body-default text-lumora-secondary">Loading decks…</p>}

          {!isLoading && !isError && (decks?.length ?? 0) === 0 && (
            <EmptyState
              icon={GraduationCap}
              message="You don't have any decks yet. Create one first, then come back here to study it."
              actionLabel="Go to Decks"
              onAction={() => router.push("/decks")}
            />
          )}

          {!isLoading && (decks?.length ?? 0) > 0 && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-[14px]">
              {decks!.map((deck) => (
                <Link
                  key={deck.id}
                  href={`/study/${deck.id}`}
                  className="bg-lumora-surface border border-lumora-border rounded-card p-5 flex flex-col gap-2 hover:bg-lumora-surface-hover transition-colors"
                >
                  <span className="text-card-heading font-semibold text-lumora-primary truncate">
                    {deck.name}
                  </span>
                  <span className="text-caption-xs text-lumora-muted">Tap to study this deck</span>
                </Link>
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
