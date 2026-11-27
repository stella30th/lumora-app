import { apiClient } from "./api-client";
import {
  ChangePasswordRequest,
  MessageResponse,
  UpdateProfileRequest,
  UserResponse,
} from "@/types/user";

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

export function verifyEmail(token: string): Promise<MessageResponse> {
  return apiClient<MessageResponse>("/api/users/verify-email", {
    method: "POST",
    skipAuth: true,
    body: JSON.stringify({ token }),
  });
}

export function resendVerification(email: string): Promise<MessageResponse> {
  return apiClient<MessageResponse>("/api/users/resend-verification", {
    method: "POST",
    skipAuth: true,
    body: JSON.stringify({ email }),
  });
}
