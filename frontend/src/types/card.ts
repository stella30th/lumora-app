// Matches backend CardResponse (backend/.../dto/card/CardResponse.java) exactly.
export interface Card {
  id: number;
  term: string; // front of the card
  definition: string; // back of the card
  deckId: number;
  createdAt: string;
}

// Matches backend CardRequest (backend/.../dto/card/CardRequest.java).
export interface CardFormInput {
  term: string;
  definition: string;
}

// Matches backend ReviewStatus enum (backend/.../entity/ReviewStatus.java).
export type ReviewStatus = "NEW" | "LEARNING" | "REVIEW";

// Matches backend DueCardResponse (backend/.../dto/cardprogress/DueCardResponse.java) --
// what GET /api/decks/{deckId}/review-queue returns. nextReviewDate is null for a
// card that has never been reviewed (status NEW, no CardProgress row yet).
export interface DueCard {
  id: number;
  term: string;
  definition: string;
  deckId: number;
  status: ReviewStatus;
  nextReviewDate: string | null;
}

// Matches backend CardProgressResponse (backend/.../dto/cardprogress/CardProgressResponse.java)
// -- what POST /api/cards/{cardId}/review returns after grading one card.
export interface CardProgressResult {
  id: number;
  cardId: number;
  easeFactor: number;
  intervalDays: number;
  repetitions: number;
  status: ReviewStatus;
  nextReviewDate: string;
  lastReviewedAt: string;
}
