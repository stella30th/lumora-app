"use client";

import React, { useEffect, useState } from "react";
import { Loader2, Pencil } from "lucide-react";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { updateLocalUser } from "@/lib/auth";
import * as usersApi from "@/lib/users";
import { UserResponse } from "@/types/user";

interface EditProfileFormProps {
  user?: UserResponse | null;
  onUpdated: (user: UserResponse) => void;
}

export function EditProfileForm({ user, onUpdated }: EditProfileFormProps) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  // Keep the form in sync whenever the loaded user changes (e.g. once the
  // GET /api/users/{id} query resolves after the page first mounts).
  useEffect(() => {
    setUsername(user?.username ?? "");
    setEmail(user?.email ?? "");
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    const trimmedUsername = username.trim();
    const trimmedEmail = email.trim();

    if (!trimmedUsername || !trimmedEmail) {
      setErrorMessage("Username and email are required.");
      return;
    }
    if (trimmedUsername.length > 50) {
      setErrorMessage("Username must not exceed 50 characters.");
      return;
    }

    setIsSubmitting(true);
    try {
      const updated = await usersApi.updateProfile({
        username: trimmedUsername,
        email: trimmedEmail,
      });

      // The JWT access token only carries the user id, so nothing on the
      // backend needs to change — we just patch the cached copy the UI
      // reads from (see Sidebar) and let the caller refresh its query.
      updateLocalUser({ username: updated.username, email: updated.email });
      onUpdated(updated);
      setSuccessMessage("Profile updated.");
    } catch (err: unknown) {
      const error = err as Error;
      setErrorMessage(error.message || "Could not update your profile.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="bg-lumora-surface border border-lumora-border rounded-card p-6 flex flex-col gap-4">
      <div className="flex items-center gap-2 text-lumora-secondary">
        <Pencil className="w-4 h-4 stroke-[1.8]" />
        <h2 className="text-card-heading font-semibold text-lumora-primary">Edit Profile</h2>
      </div>

      {errorMessage && <ErrorBanner message={errorMessage} />}
      {successMessage && (
        <div
          role="status"
          className="p-3 rounded-input border border-lumora-border bg-lumora-surface-hover text-lumora-primary text-body-default font-medium"
        >
          {successMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label
            htmlFor="profile-username"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Username
          </label>
          <input
            id="profile-username"
            type="text"
            required
            maxLength={50}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
          />
        </div>

        <div>
          <label
            htmlFor="profile-email"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Email
          </label>
          <input
            id="profile-email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
          />
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex items-center gap-2 self-start px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 disabled:opacity-50 transition-opacity"
        >
          {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
          <span>Save Changes</span>
        </button>
      </form>
    </section>
  );
}
