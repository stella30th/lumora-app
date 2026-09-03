"use client";

import React, { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import { X, Settings, CheckCircle2 } from "lucide-react";
import { Flashcard } from "@/components/study/Flashcard";
import { ReviewButtons } from "@/components/study/ReviewButtons";
import { ProgressBar } from "@/components/study/ProgressBar";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { isAuthenticated } from "@/lib/auth";
import * as reviewApi from "@/lib/review";
import * as decksApi from "@/lib/decks";
import { DueCard } from "@/types/card";

export default function StudySessionPage() {
  const router = useRouter();
  const params = useParams();
  const deckId = Number(params?.deckId);

  const [queue, setQueue] = useState<DueCard[] | null>(null);
  const [index, setIndex] = useState(0);
  const [flipped, setFlipped] = useState(false);
  const [reviewedCount, setReviewedCount] = useState(0);
  const [feedback, setFeedback] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) router.replace("/login");
  }, [router]);

  const { data: deck } = useQuery({
    queryKey: ["decks", deckId],
    queryFn: () => decksApi.getDeck(deckId),
    enabled: !!deckId,
  });

  const {
    data: dueCards,
    isLoading,
    isError,
    error,
  } = useQuery<DueCard[], Error>({
    queryKey: ["review-queue", deckId],
    queryFn: () => reviewApi.getDueCards(deckId),
    enabled: !!deckId,
  });

  useEffect(() => {
    if (dueCards && queue === null) setQueue(dueCards);
  }, [dueCards, queue]);

  const submitMutation = useMutation({
    mutationFn: ({ cardId, correct }: { cardId: number; correct: boolean }) =>
      reviewApi.submitReview(cardId, correct),
  });

  const total = queue?.length ?? 0;
  const current = queue?.[index] ?? null;
  const isDone = queue !== null && index >= total;

  const handleFlip = useCallback(() => {
    if (current) setFlipped((f) => !f);
  }, [current]);

  const handleAnswer = useCallback(
    (correct: boolean) => {
      if (!current || submitMutation.isPending) return;
      submitMutation.mutate(
        { cardId: current.id, correct },
        {
          onSuccess: (result) => {
            setFeedback(
              `"${current.term}" saved — next review in ${result.intervalDays} day${
                result.intervalDays === 1 ? "" : "s"
              }.`
            );
            setReviewedCount((c) => c + 1);
            setFlipped(false);
            setIndex((i) => i + 1);
          },
        }
      );
    },
    [current, submitMutation]
  );

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (!current) return;
      if (e.code === "Space") {
        e.preventDefault();
        handleFlip();
      } else if (flipped && e.key === "1") {
        handleAnswer(false);
      } else if (flipped && e.key === "2") {
        handleAnswer(true);
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [current, flipped, handleFlip, handleAnswer]);

  return (
    <div className="min-h-screen flex flex-col bg-lumora-bg text-lumora-primary">
      <header className="flex items-center justify-between px-6 py-4 border-b border-lumora-border">
        <Link
          href={`/decks/${deckId}`}
          aria-label="Close study session"
          className="p-2 rounded-btn text-lumora-secondary hover:text-lumora-primary hover:bg-lumora-surface-hover transition-colors"
        >
          <X className="w-5 h-5 stroke-[1.8]" />
        </Link>

        {!isDone && total > 0 && <ProgressBar current={Math.min(index + 1, total)} total={total} />}

        <button
          type="button"
          aria-label="Settings"
          disabled
          className="p-2 rounded-btn text-lumora-muted cursor-default"
        >
          <Settings className="w-5 h-5 stroke-[1.8]" />
        </button>
      </header>

      <main className="flex-1 flex flex-col items-center justify-center gap-6 p-6">
        {deck && <p className="text-caption-xs text-lumora-secondary">{deck.name}</p>}

        {isError && <ErrorBanner message={error?.message || "Could not load due cards."} />}
        {submitMutation.isError && (
          <ErrorBanner message={submitMutation.error?.message || "Could not save that answer."} />
        )}

        {isLoading && <p className="text-body-default text-lumora-secondary">Loading cards…</p>}

        {!isLoading && !isError && isDone && (
          <div className="flex flex-col items-center gap-4 text-center">
            <div className="w-14 h-14 rounded-full bg-lumora-surface border border-lumora-border flex items-center justify-center text-lumora-primary">
              <CheckCircle2 className="w-7 h-7 stroke-[1.8]" />
            </div>
            <h1 className="text-title-page font-bold">
              {total === 0 ? "Nothing due right now" : "Done!"}
            </h1>
            <p className="text-body-default text-lumora-secondary max-w-sm">
              {total === 0
                ? "This deck has no cards due for review at the moment."
                : `You reviewed ${reviewedCount} card${reviewedCount === 1 ? "" : "s"} today.`}
            </p>
            <Link
              href="/dashboard"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 transition-opacity"
            >
              Back to Dashboard
            </Link>
          </div>
        )}

        {!isLoading && !isError && !isDone && current && (
          <>
            {feedback && (
              <p className="text-caption-xs text-lumora-secondary text-center max-w-md">{feedback}</p>
            )}
            <Flashcard term={current.term} definition={current.definition} flipped={flipped} onFlip={handleFlip} />
            {flipped && <ReviewButtons onAnswer={handleAnswer} disabled={submitMutation.isPending} />}
          </>
        )}
      </main>
    </div>
  );
}
