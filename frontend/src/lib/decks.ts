import { apiClient } from "./api-client";
import { Deck, DeckFormInput } from "@/types/deck";
import { PagedResponse } from "@/types/pagination";

export function getDecks(page = 0, size = 20): Promise<PagedResponse<Deck>> {
  return apiClient<PagedResponse<Deck>>(`/api/decks?page=${page}&size=${size}`);
}

export function getDeck(id: number): Promise<Deck> {
  return apiClient<Deck>(`/api/decks/${id}`);
}

export function createDeck(input: DeckFormInput): Promise<Deck> {
  return apiClient<Deck>("/api/decks", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateDeck(id: number, input: DeckFormInput): Promise<Deck> {
  return apiClient<Deck>(`/api/decks/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteDeck(id: number): Promise<void> {
  return apiClient<void>(`/api/decks/${id}`, { method: "DELETE" });
}
