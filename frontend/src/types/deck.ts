export interface DeckSummary {
  id: number;
  title: string;
  description?: string;
  totalCards: number;
  dueCards?: number;
  updatedAt?: string;
}

export interface DashboardStats {
  totalDecks: number;
  totalCards: number;
  cardsDue: number;
  dayStreak: number;
}
