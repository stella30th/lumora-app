import { getAccessToken, getRefreshToken, updateTokens, clearAuth } from "./auth";
import { LoginResponse } from "@/types/user";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface RequestOptions extends RequestInit {
  skipAuth?: boolean;
}

// Lock to prevent multiple simultaneous refresh calls which would invalidate rotation
let isRefreshing = false;
let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    clearAuth();
    return null;
  }

  if (isRefreshing && refreshPromise) {
    return refreshPromise;
  }

  isRefreshing = true;
  refreshPromise = (async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/refresh`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (!response.ok) {
        throw new Error("Refresh token expired or invalid");
      }

      const data: LoginResponse = await response.json();
      updateTokens(data.token, data.refreshToken);
      return data.token;
    } catch {
      clearAuth();
      if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
        window.location.href = "/login?expired=1";
      }
      return null;
    } finally {
      isRefreshing = false;
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

export async function apiClient<T = unknown>(
  endpoint: string,
  options: RequestOptions = {}
): Promise<T> {
  const { skipAuth = false, headers = {}, ...rest } = options;
  const url = endpoint.startsWith("http") ? endpoint : `${API_BASE_URL}${endpoint}`;

  const requestHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    ...(headers as Record<string, string>),
  };

  if (!skipAuth) {
    const token = getAccessToken();
    if (token) {
      requestHeaders["Authorization"] = `Bearer ${token}`;
    }
  }

  let res = await fetch(url, {
    ...rest,
    headers: requestHeaders,
  });

  // Handle 401 Unauthorized by attempting token refresh
  if (res.status === 401 && !skipAuth && !endpoint.includes("/api/users/refresh")) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      requestHeaders["Authorization"] = `Bearer ${newToken}`;
      res = await fetch(url, {
        ...rest,
        headers: requestHeaders,
      });
    }
  }

  if (!res.ok) {
    let errorMessage = `HTTP error ${res.status}`;
    try {
      const errorBody = await res.json();
      errorMessage =
        errorBody.message ||
        errorBody.error ||
        (Array.isArray(errorBody.errors) ? errorBody.errors.join(", ") : errorMessage);
    } catch {
      // Body is not JSON
    }
    const err = new Error(errorMessage) as Error & { status?: number };
    err.status = res.status;
    throw err;
  }

  if (res.status === 204) {
    return {} as T;
  }

  return res.json() as Promise<T>;
}
