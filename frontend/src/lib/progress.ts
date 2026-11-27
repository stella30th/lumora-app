import { apiClient } from "./api-client";
import { ReviewStats } from "@/types/progress";

export const REVIEW_STATS_QUERY_KEY = ["progress-stats"] as const;

export function getReviewStats(): Promise<ReviewStats> {
  return apiClient<ReviewStats>("/api/progress/stats");
}
