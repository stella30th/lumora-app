import { apiClient } from "./api-client";
import { UserResponse } from "@/types/user";

export function getUserById(id: number): Promise<UserResponse> {
  return apiClient<UserResponse>(`/api/users/${id}`);
}
