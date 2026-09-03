"use client";

import React, { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { Modal } from "@/components/shared/Modal";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { Card, CardFormInput } from "@/types/card";

interface CardFormModalProps {
  open: boolean;
  card?: Card | null;
  onClose: () => void;
  onSubmit: (input: CardFormInput) => Promise<void>;
}

export function CardFormModal({ open, card, onClose, onSubmit }: CardFormModalProps) {
  const isEditMode = !!card;
  const [term, setTerm] = useState("");
  const [definition, setDefinition] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (open) {
      setTerm(card?.term ?? "");
      setDefinition(card?.definition ?? "");
      setErrorMessage("");
    }
  }, [open, card]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!term.trim() || !definition.trim()) {
      setErrorMessage("Both the front and back of the card are required.");
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit({ term: term.trim(), definition: definition.trim() });
    } catch (err: unknown) {
      const error = err as Error;
      setErrorMessage(error.message || "Something went wrong. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title={isEditMode ? "Edit Card" : "Add Card"}>
      {errorMessage && (
        <div className="mb-4">
          <ErrorBanner message={errorMessage} />
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label
            htmlFor="card-term"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Front
          </label>
          <textarea
            id="card-term"
            rows={2}
            required
            maxLength={255}
            placeholder="Word or question"
            value={term}
            onChange={(e) => setTerm(e.target.value)}
            className="w-full bg-lumora-bg border border-lumora-border rounded-input px-3.5 py-2.5 text-body-default text-lumora-primary placeholder:text-lumora-muted focus:outline-none focus:border-lumora-primary transition-colors resize-none"
          />
        </div>

        <div>
          <label
            htmlFor="card-definition"
            className="block text-caption-xs font-semibold uppercase tracking-wider text-lumora-secondary mb-1.5"
          >
            Back
          </label>
          <textarea
            id="card-definition"
            rows={3}
            required
            maxLength={1000}
            placeholder="Meaning or answer"
            value={definition}
            onChange={(e) => setDefinition(e.target.value)}
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
