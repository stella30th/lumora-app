"use client";

import React, { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { Modal } from "@/components/shared/Modal";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { Deck, DeckFormInput } from "@/types/deck";

interface DeckFormModalProps {
  open: boolean;
  deck?: Deck | null;
  onClose: () => void;
  onSubmit: (input: DeckFormInput) => Promise<void>;
}

export function DeckFormModal({ open, deck, onClose, onSubmit }: DeckFormModalProps) {
  const isEditMode = !!deck;
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (open) {
      setName(deck?.name ?? "");
      setDescription(deck?.description ?? "");
      setErrorMessage("");
    }
  }, [open, deck]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!name.trim()) {
      setErrorMessage("Deck name is required.");
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit({
        name: name.trim(),
        description: description.trim() || undefined,
        isPublic: deck?.isPublic ?? false,
      });
    } catch (err: unknown) {
      const error = err as Error;
      setErrorMessage(error.message || "Something went wrong. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title={isEditMode ? "Edit Deck" : "Create Deck"}>
      {errorMessage && (
        <div className="mb-4">
          <ErrorBanner message={errorMessage} />
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label
            htmlFor="deck-name"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Deck Name
          </label>
          <input
            id="deck-name"
            type="text"
            required
            maxLength={100}
            placeholder="e.g. IELTS Vocabulary"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors"
          />
        </div>

        <div>
          <label
            htmlFor="deck-description"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Description (optional)
          </label>
          <textarea
            id="deck-description"
            rows={3}
            maxLength={500}
            placeholder="What is this deck for?"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors resize-none"
          />
        </div>

        <div className="flex items-center justify-end gap-3 mt-2">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="px-4 py-2 rounded-btn border border-lumora-border bg-lumora-surface text-lumora-primary hover:bg-lumora-surface-hover transition-colors font-semibold text-body-default disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 disabled:opacity-50 transition-opacity"
          >
            {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
            <span>Save</span>
          </button>
        </div>
      </form>
    </Modal>
  );
}
