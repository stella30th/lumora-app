"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Layers, CreditCard, Clock, Flame, ArrowRight } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { StatTile } from "@/components/dashboard/StatTile";
import { DeckCard } from "@/components/dashboard/DeckCard";
import { StudyNowCard } from "@/components/dashboard/StudyNowCard";
import { getUser, isAuthenticated } from "@/lib/auth";
import { DeckSummary, DashboardStats } from "@/types/deck";

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

  // TODO: Wire up the real API on Day 2 once the deck/card/review endpoints are ready
  const [stats] = useState<DashboardStats>({
    totalDecks: 4,
    totalCards: 142,
    cardsDue: 18,
    dayStreak: 5,
  });

  // TODO: Wire up the real deck list API on Day 2
  const [recentDecks] = useState<DeckSummary[]>([
    {
      id: 1,
      title: "3000 Common Oxford Words",
      description: "The most essential everyday vocabulary for daily English communication.",
      totalCards: 60,
      dueCards: 8,
      updatedAt: "2 hours ago",
    },
    {
      id: 2,
      title: "Software Engineering Terms",
      description: "System architecture terminology, design patterns, and databases.",
      totalCards: 45,
      dueCards: 6,
      updatedAt: "Yesterday",
    },
    {
      id: 3,
      title: "Common Idioms & Phrasal Verbs",
      description: "Natural English idioms for working professionals.",
      totalCards: 25,
      dueCards: 4,
      updatedAt: "3 days ago",
    },
    {
      id: 4,
      title: "IELTS Band 7.5+ Vocabulary",
      description: "Advanced academic vocabulary for Writing Task 2.",
      totalCards: 12,
      dueCards: 0,
      updatedAt: "5 days ago",
    },
  ]);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }

    const user = getUser();
    const currentName = user?.username || "there";
    setUserName(currentName);

    // Pick random sarcasm greeting
    const randomIndex = Math.floor(Math.random() * SARCASM_GREETINGS.length);
    setGreeting(
      SARCASM_GREETINGS[randomIndex](currentName, stats.cardsDue, stats.dayStreak)
    );
  }, [router, stats.cardsDue, stats.dayStreak]);

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      {/* Sidebar 240px */}
      <Sidebar />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />

        <main className="p-8 max-w-7xl w-full mx-auto flex flex-col gap-8">
          {/* Greeting Section */}
          <div className="flex flex-col gap-1.5">
            <h1 className="text-title-lg font-bold text-lumora-primary tracking-tight">
              {greeting || `Hey ${userName}, look who decided to show up?`}
            </h1>
            <p className="text-body-default text-lumora-secondary">
              Review your decks and continue today&apos;s study session to keep your streak alive.
            </p>
          </div>

          {/* 4 Stat Tiles */}
          <section aria-label="Overview stats">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[14px]">
              <StatTile
                label="Total Decks"
                value={stats.totalDecks}
                subtext="decks"
                icon={Layers}
              />
              <StatTile
                label="Total Cards"
                value={stats.totalCards}
                subtext="cards"
                icon={CreditCard}
              />
              <StatTile
                label="Cards Due"
                value={stats.cardsDue}
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

          {/* Study Now Banner */}
          <section aria-label="Continue studying">
            <StudyNowCard
              deckId={recentDecks[0]?.id || 1}
              deckTitle={recentDecks[0]?.title || "3000 Common Oxford Words"}
              dueCards={stats.cardsDue}
            />
          </section>

          {/* Your Decks Section */}
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

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[14px]">
              {recentDecks.map((deck) => (
                <DeckCard key={deck.id} deck={deck} />
              ))}
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}
