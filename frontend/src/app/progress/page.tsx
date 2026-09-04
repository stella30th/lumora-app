"use client";

import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQueries, useQuery } from "@tanstack/react-query";
import { Clock, Layers, BarChart3 } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { StatTile } from "@/components/dashboard/StatTile";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { isAuthenticated } from "@/lib/auth";
import * as decksApi from "@/lib/decks";
import * as reviewApi from "@/lib/review";
import { Deck } from "@/types/deck";
import { DueCard } from "@/types/card";

export default function ProgressPage() {
  const router = useRouter();

  useEffect(() => {
    if (!isAuthenticated()) router.replace("/login");
  }, [router]);

  const {
    data: decks,
    isLoading: decksLoading,
    isError,
    error,
  } = useQuery<Deck[], Error>({
    queryKey: ["decks"],
    queryFn: decksApi.getDecks,
  });

  const dueQueries = useQueries({
    queries: (decks ?? []).map((deck) => ({
      queryKey: ["review-queue", deck.id],
      queryFn: () => reviewApi.getDueCards(deck.id),
      enabled: !!decks,
    })),
  });

  const totalDue = dueQueries.reduce(
    (sum, q) => sum + ((q.data as DueCard[] | undefined)?.length ?? 0),
    0
  );
  const dueLoading = decksLoading || dueQueries.some((q) => q.isLoading);

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />
        <main className="p-8 max-w-7xl w-full mx-auto flex flex-col gap-8">
          <div>
            <h1 className="text-title-page font-bold">Progress</h1>
            <p className="text-body-default text-lumora-secondary mt-1">
              What Lumora can measure right now, from data the backend already tracks.
            </p>
          </div>

          {isError && <ErrorBanner message={error?.message || "Could not load your decks."} />}

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-[14px]">
            <StatTile
              label="Total Decks"
              value={decksLoading ? "…" : decks?.length ?? 0}
              subtext="decks"
              icon={Layers}
            />
            <StatTile
              label="Cards Due Now"
              value={dueLoading ? "…" : totalDue}
              subtext="across all decks"
              icon={Clock}
            />
          </div>

          <EmptyState
            icon={BarChart3}
            message="Streaks and review history aren't available yet — the backend only stores each card's current progress, not a log of past reviews. That needs a new endpoint (and probably a new table) on the backend first."
          />
        </main>
      </div>
    </div>
  );
}
