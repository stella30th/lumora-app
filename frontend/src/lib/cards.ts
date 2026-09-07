import { apiClient } from "./api-client";
import { Card, CardFormInput } from "@/types/card";
import { PagedResponse } from "@/types/pagination";

export function getCardsByDeck(deckId: number, page = 0, size = 20): Promise<PagedResponse<Card>> {
  return apiClient<PagedResponse<Card>>(`/api/decks/${deckId}/cards?page=${page}&size=${size}`);
}

export function createCard(deckId: number, input: CardFormInput): Promise<Card> {
  return apiClient<Card>(`/api/decks/${deckId}/cards`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateCard(id: number, input: CardFormInput): Promise<Card> {
  return apiClient<Card>(`/api/cards/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteCard(id: number): Promise<void> {
  return apiClient<void>(`/api/cards/${id}`, { method: "DELETE" });
}
