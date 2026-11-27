export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
}

export interface MessageResponse {
  message: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  pendingEmail?: string | null;
  createdAt?: string;
}

export interface UserSession {
  userId: number;
  username: string;
  email?: string;
}

export interface UpdateProfileRequest {
  username: string;
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
