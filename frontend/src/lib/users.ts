import { apiClient } from "./api-client";
import { UserResponse, UpdateProfileRequest, ChangePasswordRequest } from "@/types/user";

export function getCurrentUser(): Promise<UserResponse> {
  return apiClient<UserResponse>("/api/users/me");
}

export function updateProfile(data: UpdateProfileRequest): Promise<UserResponse> {
  return apiClient<UserResponse>("/api/users/me", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export function changePassword(data: ChangePasswordRequest): Promise<void> {
  return apiClient<void>("/api/users/me/password", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}
