export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  userId: number;
  username: string;
}

export interface UserRegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  createdAt?: string;
}

export interface UserSession {
  userId: number;
  username: string;
  email?: string;
}
