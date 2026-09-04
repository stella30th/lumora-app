"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQueries, useQuery } from "@tanstack/react-query";
import { Layers, CreditCard, Clock, Flame, ArrowRight, Plus } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { StatTile } from "@/components/dashboard/StatTile";
import { DeckCard } from "@/components/dashboard/DeckCard";
import { StudyNowCard } from "@/components/dashboard/StudyNowCard";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { getUser, isAuthenticated } from "@/lib/auth";
import * as decksApi from "@/lib/decks";
import * as cardsApi from "@/lib/cards";
import * as reviewApi from "@/lib/review";
import { Deck, DashboardStats } from "@/types/deck";
import { Card, DueCard } from "@/types/card";

const SARCASM_GREETINGS = [
  (name: string, _due: number, streak: number) =>
    `Hey ${name}, look who decided to show up. That ${streak}-day streak is hanging by a thread.`,
  (_name: string, due: number) =>
    `${due} cards are waiting. They won't disappear on their own.`,
  (name: string) =>
    `Yesterday ${name} promised to study. What's the plan today?`,
  (name: string) =>
    `Hey ${name}, your brain is quietly rotting away.`,
  (name: string) =>
    `Lumora deals only in truth: ${name}, are you behind on your reviews?`,
];

export default function DashboardPage() {
  const router = useRouter();
  const [userName, setUserName] = useState("there");
  const [greeting, setGreeting] = useState("");

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    setUserName(getUser()?.username || "there");
  }, [router]);

  const {
    data: decks,
    isLoading: areDecksLoading,
    isError: isDecksError,
    error: decksError,
  } = useQuery<Deck[], Error>({
    queryKey: ["decks"],
    queryFn: decksApi.getDecks,
  });

  const cardCountQueries = useQueries({
    queries: (decks ?? []).map((deck) => ({
      queryKey: ["cards", deck.id],
      queryFn: () => cardsApi.getCardsByDeck(deck.id),
      enabled: !!decks,
    })),
  });

  const dueQueries = useQueries({
    queries: (decks ?? []).map((deck) => ({
      queryKey: ["review-queue", deck.id],
      queryFn: () => reviewApi.getDueCards(deck.id),
      enabled: !!decks,
    })),
  });

  const cardCountByDeckId = new Map<number, number | undefined>();
  (decks ?? []).forEach((deck, i) => {
    cardCountByDeckId.set(deck.id, (cardCountQueries[i]?.data as Card[] | undefined)?.length);
  });

  const totalCards = cardCountQueries.reduce(
    (sum, q) => sum + ((q.data as Card[] | undefined)?.length ?? 0),
    0
  );
  const totalDue = dueQueries.reduce(
    (sum, q) => sum + ((q.data as DueCard[] | undefined)?.length ?? 0),
    0
  );
  const isDueLoading = areDecksLoading || dueQueries.some((q) => q.isLoading);

  const recentDecks = [...(decks ?? [])]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 4);

  const stats: DashboardStats = {
    totalDecks: decks?.length ?? 0,
    totalCards,
    cardsDue: totalDue,
    dayStreak: 5,
  };

  useEffect(() => {
    if (!userName) return;
    const randomIndex = Math.floor(Math.random() * SARCASM_GREETINGS.length);
    setGreeting(SARCASM_GREETINGS[randomIndex](userName, stats.cardsDue, stats.dayStreak));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userName]);

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      <Sidebar />

      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />

        <main className="p-8 max-w-7xl w-full mx-auto flex flex-col gap-8">
          <div className="flex flex-col gap-1.5">
            <h1 className="text-title-lg font-bold text-lumora-primary tracking-tight">
              {greeting || `Hey ${userName}, look who decided to show up?`}
            </h1>
            <p className="text-body-default text-lumora-secondary">
              Review your decks and continue today&apos;s study session to keep your streak alive.
            </p>
          </div>

          {isDecksError && (
            <ErrorBanner message={decksError?.message || "Could not load your decks."} />
          )}

          <section aria-label="Overview stats">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[14px]">
              <StatTile
                label="Total Decks"
                value={areDecksLoading ? "…" : stats.totalDecks}
                subtext="decks"
                icon={Layers}
              />
              <StatTile
                label="Total Cards"
                value={areDecksLoading ? "…" : stats.totalCards}
                subtext="cards"
                icon={CreditCard}
              />
              <StatTile
                label="Cards Due"
                value={isDueLoading ? "…" : stats.cardsDue}
                subtext="to review"
                icon={Clock}
              />
              <StatTile
                label="Day Streak"
                value={`${stats.dayStreak}d`}
                subtext="in a row"
                icon={Flame}
              />
            </div>
          </section>

          {recentDecks.length > 0 && (
            <section aria-label="Continue studying">
              <StudyNowCard
                deckId={recentDecks[0].id}
                deckTitle={recentDecks[0].name}
                dueCards={stats.cardsDue}
              />
            </section>
          )}

          <section aria-label="Recent decks" className="flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-title-page font-bold text-lumora-primary">
                  Your Decks
                </h2>
                <p className="text-body-default text-lumora-secondary mt-0.5">
                  The decks you&apos;ve studied most recently
                </p>
              </div>

              <Link
                href="/decks"
                className="inline-flex items-center gap-1.5 text-body-default font-semibold text-lumora-secondary hover:text-lumora-primary transition-colors"
              >
                <span>View all</span>
                <ArrowRight className="w-4 h-4 stroke-[1.8]" />
              </Link>
            </div>

            {areDecksLoading && (
              <p className="text-body-default text-lumora-secondary">Loading decks...</p>
            )}

            {!areDecksLoading && recentDecks.length === 0 && !isDecksError && (
              <Link
                href="/decks"
                className="inline-flex items-center gap-2 self-start px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 transition-opacity"
              >
                <Plus className="w-4 h-4 stroke-[2]" />
                <span>Create your first deck</span>
              </Link>
            )}

            {recentDecks.length > 0 && (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[14px]">
                {recentDecks.map((deck) => (
                  <DeckCard key={deck.id} deck={deck} cardCount={cardCountByDeckId.get(deck.id)} />
                ))}
              </div>
            )}
          </section>
        </main>
      </div>
    </div>
  );
}
