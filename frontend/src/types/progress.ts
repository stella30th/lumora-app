export interface DailyReviewCount {
  date: string;
  correctCount: number;
  incorrectCount: number;
}

export interface ReviewStats {
  currentStreakDays: number;
  reviewsToday: number;
  history: DailyReviewCount[];
}
