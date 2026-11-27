import { useMemo, useSyncExternalStore } from "react";
import { UserSession } from "@/types/user";
import { clearAllQueryCache } from "./query-client-ref";

const LEGACY_ACCESS_TOKEN_KEY = "lumora_access_token";
const LEGACY_REFRESH_TOKEN_KEY = "lumora_refresh_token";

const USER_KEY = "lumora_user";

let accessToken: string | null = null;

export function getAccessToken(): string | null {
  return accessToken;
}

function removeLegacyTokens(): void {
  localStorage.removeItem(LEGACY_ACCESS_TOKEN_KEY);
  localStorage.removeItem(LEGACY_REFRESH_TOKEN_KEY);
}

const USER_CHANGED_EVENT = "lumora-user-updated";

function parseUser(raw: string | null): UserSession | null {
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function notifyUserChanged(): void {
  window.dispatchEvent(new Event(USER_CHANGED_EVENT));
}

export function getUser(): UserSession | null {
  if (typeof window === "undefined") return null;
  return parseUser(localStorage.getItem(USER_KEY));
}

function subscribeToUser(onChange: () => void): () => void {
  window.addEventListener(USER_CHANGED_EVENT, onChange);
  window.addEventListener("storage", onChange);
  return () => {
    window.removeEventListener(USER_CHANGED_EVENT, onChange);
    window.removeEventListener("storage", onChange);
  };
}

export function useStoredUser(): UserSession | null {
  const raw = useSyncExternalStore(
    subscribeToUser,
    () => localStorage.getItem(USER_KEY),
    () => null
  );
  return useMemo(() => parseUser(raw), [raw]);
}

export function setAuth(data: {
  token: string;
  userId: number;
  username: string;
  email?: string;
}): void {
  if (typeof window === "undefined") return;

  clearAllQueryCache();
  accessToken = data.token;
  removeLegacyTokens();
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      userId: data.userId,
      username: data.username,
      email: data.email || "",
    })
  );
  notifyUserChanged();
}

export function updateAccessToken(token: string): void {
  if (typeof window === "undefined") return;
  accessToken = token;
}

export function clearAuth(): void {
  if (typeof window === "undefined") return;
  accessToken = null;
  removeLegacyTokens();
  localStorage.removeItem(USER_KEY);
  clearAllQueryCache();
  notifyUserChanged();
}

export function isAuthenticated(): boolean {
  return !!getUser();
}

export function updateLocalUser(patch: Partial<Pick<UserSession, "username" | "email">>): void {
  if (typeof window === "undefined") return;
  const current = getUser();
  if (!current) return;
  const updated: UserSession = { ...current, ...patch };
  localStorage.setItem(USER_KEY, JSON.stringify(updated));
  notifyUserChanged();
}
