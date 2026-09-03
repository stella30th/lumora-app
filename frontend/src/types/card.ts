export interface Card {
  id: number;
  term: string;
  definition: string;
  deckId: number;
  createdAt: string;
}

export interface CardFormInput {
  term: string;
  definition: string;
}

export type ReviewStatus = "NEW" | "LEARNING" | "REVIEW";

export interface DueCard {
  id: number;
  term: string;
  definition: string;
  deckId: number;
  status: ReviewStatus;
  nextReviewDate: string | null;
}

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
