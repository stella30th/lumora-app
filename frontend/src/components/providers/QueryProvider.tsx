"use client";

import React, { useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { registerQueryClient } from "@/lib/query-client-ref";
import { SESSION_EXPIRED_EVENT } from "@/lib/api-client";

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            refetchOnWindowFocus: false,
            retry: 1,
            staleTime: 1000 * 60 * 5,
          },
        },
      })
  );

  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    registerQueryClient(queryClient);
  }, [queryClient]);

  useEffect(() => {
    const handleSessionExpired = () => {
      if (!pathname.startsWith("/login")) {
        router.replace("/login?expired=1");
      }
    };
    window.addEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);
  }, [router, pathname]);

  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}
