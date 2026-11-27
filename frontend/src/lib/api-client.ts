import { getAccessToken, updateAccessToken, clearAuth, getUser } from "./auth";
import { LoginResponse } from "@/types/user";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface RequestOptions extends RequestInit {
  skipAuth?: boolean;
}

export const SESSION_EXPIRED_EVENT = "lumora-session-expired";

let isRefreshing = false;
let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  if (isRefreshing && refreshPromise) {
    return refreshPromise;
  }

  isRefreshing = true;
  refreshPromise = (async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/refresh`, {
        method: "POST",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Refresh token expired or invalid");
      }

      const data: LoginResponse = await response.json();
      updateAccessToken(data.token);
      return data.token;
    } catch {
      clearAuth();
      if (typeof window !== "undefined") {
        window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
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

  if (!skipAuth && !getAccessToken() && getUser()) {
    await refreshAccessToken();
  }

  if (!skipAuth) {
    const token = getAccessToken();
    if (token) {
      requestHeaders["Authorization"] = `Bearer ${token}`;
    }
  }

  let res = await fetch(url, {
    ...rest,
    headers: requestHeaders,
    credentials: "include",
  });

  if (res.status === 401 && !skipAuth && !endpoint.includes("/api/users/refresh")) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      requestHeaders["Authorization"] = `Bearer ${newToken}`;
      res = await fetch(url, {
        ...rest,
        headers: requestHeaders,
        credentials: "include",
      });
    }
  }

  if (!res.ok) {
    let errorMessage = `HTTP error ${res.status}`;
    try {
      const errorBody = await res.json();
      const fieldMessages =
        errorBody.errors && typeof errorBody.errors === "object"
          ? Array.isArray(errorBody.errors)
            ? errorBody.errors
            : Object.values(errorBody.errors)
          : [];
      if (fieldMessages.length > 0) {
        errorMessage = fieldMessages.join(", ");
      } else if (typeof errorBody.message === "string") {
        errorMessage = errorBody.message;
      } else if (typeof errorBody.error === "string") {
        errorMessage = errorBody.error;
      }
    } catch {
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

export async function logoutAndClear(): Promise<void> {
  try {
    await apiClient("/api/users/logout", { method: "POST" });
  } catch {
  } finally {
    clearAuth();
  }
}
