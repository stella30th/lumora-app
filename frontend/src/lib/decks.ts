// Thin wrapper functions around apiClient for the Deck endpoints
// (backend/.../controller/DeckController.java). Keeping these here instead of
// calling apiClient directly from every component means: (1) the URL/method for
// each operation is written once, (2) React Query hooks in the pages stay small
// and readable, (3) if a route ever changes, only this file needs updating.
import { apiClient } from "./api-client";
import { Deck, DeckFormInput } from "@/types/deck";

export function getDecks(): Promise<Deck[]> {
  return apiClient<Deck[]>("/api/decks");
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
