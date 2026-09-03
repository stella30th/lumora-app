export interface Deck {
  id: number;
  name: string;
  description?: string;
  isPublic: boolean;
  ownerId: number;
  createdAt: string;
}

export interface DeckFormInput {
  name: string;
  description?: string;
  isPublic: boolean;
}

export interface DashboardStats {
  totalDecks: number;
  totalCards: number;
  cardsDue: number;
  dayStreak: number;
}
