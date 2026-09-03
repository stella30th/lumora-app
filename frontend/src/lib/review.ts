import { apiClient } from "./api-client";
import { DueCard, CardProgressResult } from "@/types/card";

export function getDueCards(deckId: number): Promise<DueCard[]> {
  return apiClient<DueCard[]>(`/api/decks/${deckId}/review-queue`);
}

export function submitReview(cardId: number, correct: boolean): Promise<CardProgressResult> {
  return apiClient<CardProgressResult>(`/api/cards/${cardId}/review`, {
    method: "POST",
    body: JSON.stringify({ correct }),
  });
}
