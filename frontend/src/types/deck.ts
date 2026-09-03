// Matches backend DeckResponse (backend/.../dto/deck/DeckResponse.java) exactly.
// NOTE: the backend does NOT return a card count or an "updatedAt" timestamp for
// a deck (only createdAt). Anywhere the UI needs a card count or a relative time,
// it is fetched/derived separately -- see lib/decks.ts and lib/format.ts.
export interface Deck {
  id: number;
  name: string;
  description?: string;
  isPublic: boolean;
  ownerId: number;
  createdAt: string; // ISO datetime string (Java LocalDateTime serialized by Jackson)
}

// Matches backend DeckRequest (backend/.../dto/deck/DeckRequest.java) -- the shape
// sent on create/update. isPublic is always sent as false for Day 2: the spec
// intentionally has no UI for making a deck public yet.
export interface DeckFormInput {
  name: string;
  description?: string;
  isPublic: boolean;
}

// Day 1 dashboard stats. totalDecks/totalCards are wired to the real API from
// Day 2 onward. cardsDue/dayStreak need CardProgressService's getDueCards() /
// a streak calculation, which don't exist on the frontend yet -- that's Day 3's
// job (Study Session + Progress), so they stay hardcoded placeholders for now.
export interface DashboardStats {
  totalDecks: number;
  totalCards: number;
  cardsDue: number;
  dayStreak: number;
}
