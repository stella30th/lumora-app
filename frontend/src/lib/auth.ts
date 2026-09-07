import { UserSession } from "@/types/user";
import { clearAllQueryCache } from "./query-client-ref";

const ACCESS_TOKEN_KEY = "lumora_access_token";
const REFRESH_TOKEN_KEY = "lumora_refresh_token";
const USER_KEY = "lumora_user";

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function getUser(): UserSession | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function setAuth(data: {
  token: string;
  refreshToken: string;
  userId: number;
  username: string;
  email?: string;
}): void {
  if (typeof window === "undefined") return;
  // A different identity may be logging in on top of a still-warm cache
  // (e.g. logging back in right after logout, in the same tab) - flush it
  // so nothing from the previous session leaks into this one.
  clearAllQueryCache();
  localStorage.setItem(ACCESS_TOKEN_KEY, data.token);
  localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      userId: data.userId,
      username: data.username,
      email: data.email || "",
    })
  );
}

export function updateTokens(token: string, refreshToken: string): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function clearAuth(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  // Every logout / expired-session / password-change path funnels through
  // here - this is the one place that reliably knows "this identity is no
  // longer logged in", so it's the right spot to drop every cached query
  // (decks, cards, user info, ...) rather than leaving it for the next
  // user to accidentally see.
  clearAllQueryCache();
}

export function isAuthenticated(): boolean {
  return !!getAccessToken();
}

/**
 * Patch the cached user (username/email) after a profile update, without
 * needing a brand-new token pair. Fires a "lumora-user-updated" window
 * event so already-mounted components (like the Sidebar) can refresh
 * without a full page reload.
 */
export function updateLocalUser(patch: Partial<Pick<UserSession, "username" | "email">>): void {
  if (typeof window === "undefined") return;
  const current = getUser();
  if (!current) return;
  const updated: UserSession = { ...current, ...patch };
  localStorage.setItem(USER_KEY, JSON.stringify(updated));
  window.dispatchEvent(new Event("lumora-user-updated"));
}
