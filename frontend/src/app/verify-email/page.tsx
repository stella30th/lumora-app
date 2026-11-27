"use client";

import React, { Suspense } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { CheckCircle2, Lightbulb, Loader2 } from "lucide-react";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import * as usersApi from "@/lib/users";
import { MessageResponse } from "@/types/user";

function VerifyEmailContent() {
  const token = useSearchParams().get("token") ?? "";

  const { data, error, isPending } = useQuery<MessageResponse, Error>({
    queryKey: ["verify-email", token],
    queryFn: () => usersApi.verifyEmail(token),
    enabled: token.length > 0,
    retry: false,
    staleTime: Infinity,
    gcTime: Infinity,
  });

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-lumora-bg p-4 relative">
      <div className="absolute top-4 right-4">
        <ThemeToggle />
      </div>
      <div className="w-full max-w-[400px] bg-lumora-surface border border-lumora-border rounded-card p-8 shadow-sm flex flex-col gap-5">
        <div className="flex items-center gap-2.5">
          <div className="w-[26px] h-[26px] rounded-[7px] bg-lumora-primary flex items-center justify-center text-lumora-bg">
            <Lightbulb className="w-[15px] h-[15px] stroke-[2.2]" />
          </div>
          <span className="font-bold text-[18px] tracking-tight text-lumora-primary">Lumora</span>
        </div>

        <h1 className="text-title-page font-bold text-lumora-primary">Email verification</h1>

        {!token && <ErrorBanner message="This link is missing its token. Open the link from your email again." />}

        {token && isPending && (
          <p className="flex items-center gap-2 text-body-default text-lumora-secondary">
            <Loader2 className="w-4 h-4 animate-spin" /> Verifying your email…
          </p>
        )}

        {error && <ErrorBanner message={error.message || "This link is invalid or has expired."} />}

        {data && (
          <p className="flex items-start gap-2 text-body-default text-lumora-primary font-medium">
            <CheckCircle2 className="w-5 h-5 shrink-0 stroke-[1.8]" />
            {data.message}
          </p>
        )}

        <Link
          href="/login"
          className="inline-flex items-center justify-center w-full py-2.5 px-4 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 transition-opacity"
        >
          Go to Log In
        </Link>
      </div>
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-lumora-bg" />}>
      <VerifyEmailContent />
    </Suspense>
  );
}
