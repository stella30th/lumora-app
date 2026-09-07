import type { QueryClient } from "@tanstack/react-query";

/**
 * Holds a reference to the app's single QueryClient instance.
 *
 * QueryProvider (mounted once in the root layout) creates the QueryClient
 * and registers it here. That client's cache lives for the whole browser
 * tab and survives every client-side navigation - Next.js's App Router
 * does NOT remount the root layout when you go from /login to /dashboard
 * to /decks, it only remounts on a hard reload (F5).
 *
 * That's exactly why switching accounts without a hard refresh used to
 * show the previous user's decks: react-query cached them under a plain
 * key like ["decks", 0] with a 5-minute staleTime, so when a different
 * user logged in and hit the same key, it served the old, still-"fresh"
 * cached data instead of refetching.
 *
 * lib/auth.ts is a plain module (no React, can't call useQueryClient()),
 * but it IS the single place every login/logout/session-expiry flows
 * through. So instead of remembering to call queryClient.clear() at every
 * call site that changes who's logged in, we register the client here
 * once and let auth.ts flush the cache itself whenever identity changes.
 */
let queryClientRef: QueryClient | null = null;

export function registerQueryClient(client: QueryClient): void {
  queryClientRef = client;
}

export function clearAllQueryCache(): void {
  queryClientRef?.clear();
}
