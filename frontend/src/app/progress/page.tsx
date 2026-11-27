"use client";

import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Clock, Flame, Layers, CheckCircle2 } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { StatTile } from "@/components/dashboard/StatTile";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { isAuthenticated } from "@/lib/auth";
import * as decksApi from "@/lib/decks";
import { getReviewStats, REVIEW_STATS_QUERY_KEY } from "@/lib/progress";
import { DeckOverview } from "@/types/deck";
import { ReviewStats } from "@/types/progress";

function ReviewHistoryChart({ stats }: { stats: ReviewStats }) {
  const max = Math.max(1, ...stats.history.map((d) => d.correctCount + d.incorrectCount));
  const total = stats.history.reduce((sum, d) => sum + d.correctCount + d.incorrectCount, 0);

  return (
    <section className="bg-lumora-surface border border-lumora-border rounded-card p-6 flex flex-col gap-4">
      <div className="flex items-baseline justify-between gap-4">
        <h2 className="text-card-heading font-semibold text-lumora-primary">Last 30 days</h2>
        <span className="text-caption-xs text-lumora-muted">{total} reviews</span>
      </div>
      <div className="flex items-end gap-[3px] h-40" role="img" aria-label="Reviews per day for the last 30 days">
        {stats.history.map((day) => {
          const count = day.correctCount + day.incorrectCount;
          const correctPct = (day.correctCount / max) * 100;
          const incorrectPct = (day.incorrectCount / max) * 100;
          return (
            <div
              key={day.date}
              className="flex-1 h-full flex flex-col justify-end"
              title={`${day.date}: ${day.correctCount} remembered, ${day.incorrectCount} forgotten`}
            >
              {count === 0 ? (
                <div className="h-[2px] bg-lumora-border rounded-sm" />
              ) : (
                <>
                  <div className="bg-lumora-muted rounded-t-sm" style={{ height: `${incorrectPct}%` }} />
                  <div
                    className={`bg-lumora-primary ${day.incorrectCount === 0 ? "rounded-t-sm" : ""}`}
                    style={{ height: `${correctPct}%` }}
                  />
                </>
              )}
            </div>
          );
        })}
      </div>
      <div className="flex items-center gap-4 text-caption-xs text-lumora-secondary">
        <span className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-sm bg-lumora-primary" /> Remembered
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-sm bg-lumora-muted" /> Forgotten (Again)
        </span>
      </div>
    </section>
  );
}

export default function ProgressPage() {
  const router = useRouter();

  useEffect(() => {
    if (!isAuthenticated()) router.replace("/login");
  }, [router]);

  const {
    data: decks,
    isLoading: decksLoading,
    isError: isDecksError,
    error: decksError,
  } = useQuery<DeckOverview[], Error>({
    queryKey: decksApi.DECK_OVERVIEW_QUERY_KEY,
    queryFn: decksApi.getDeckOverview,
  });

  const {
    data: stats,
    isLoading: statsLoading,
    isError: isStatsError,
    error: statsError,
  } = useQuery<ReviewStats, Error>({
    queryKey: REVIEW_STATS_QUERY_KEY,
    queryFn: getReviewStats,
  });

  const totalDue = (decks ?? []).reduce((sum, d) => sum + d.dueCount, 0);

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />
        <main className="p-8 max-w-7xl w-full mx-auto flex flex-col gap-8">
          <div>
            <h1 className="text-title-page font-bold">Progress</h1>
            <p className="text-body-default text-lumora-secondary mt-1">
              Your study streak and review activity.
            </p>
          </div>

          {isDecksError && <ErrorBanner message={decksError?.message || "Could not load your decks."} />}
          {isStatsError && <ErrorBanner message={statsError?.message || "Could not load your statistics."} />}

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[14px]">
            <StatTile
              label="Day Streak"
              value={statsLoading ? "…" : `${stats?.currentStreakDays ?? 0}d`}
              subtext="in a row"
              icon={Flame}
            />
            <StatTile
              label="Reviewed Today"
              value={statsLoading ? "…" : stats?.reviewsToday ?? 0}
              subtext="cards"
              icon={CheckCircle2}
            />
            <StatTile
              label="Cards Due Now"
              value={decksLoading ? "…" : totalDue}
              subtext="across all decks"
              icon={Clock}
            />
            <StatTile
              label="Total Decks"
              value={decksLoading ? "…" : decks?.length ?? 0}
              subtext="decks"
              icon={Layers}
            />
          </div>

          {stats && <ReviewHistoryChart stats={stats} />}
        </main>
      </div>
    </div>
  );
}
