"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus, Folder } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { DeckCard } from "@/components/dashboard/DeckCard";
import { DeckFormModal } from "@/components/decks/DeckFormModal";
import { EmptyState } from "@/components/shared/EmptyState";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { isAuthenticated } from "@/lib/auth";
import * as decksApi from "@/lib/decks";
import * as cardsApi from "@/lib/cards";
import { Deck, DeckFormInput } from "@/types/deck";
import { Card } from "@/types/card";

// Wraps DeckCard with its own card-count fetch. One useQuery per deck instead
// of one big fan-out request: each row caches/refetches independently, and it
// keeps DeckCard itself free of any fetching concerns (it just renders props).
function DeckGridItem({
  deck,
  onEdit,
  onDelete,
}: {
  deck: Deck;
  onEdit: () => void;
  onDelete: () => void;
}) {
  const { data: cards } = useQuery<Card[], Error>({
    queryKey: ["cards", deck.id],
    queryFn: () => cardsApi.getCardsByDeck(deck.id),
  });

  return (
    <DeckCard deck={deck} cardCount={cards?.length} onEdit={onEdit} onDelete={onDelete} />
  );
}

export default function DecksPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
    }
  }, [router]);

  const {
    data: decks,
    isLoading,
    isError,
    error,
  } = useQuery<Deck[], Error>({
    queryKey: ["decks"],
    queryFn: decksApi.getDecks,
  });

  const [formOpen, setFormOpen] = useState(false);
  const [editingDeck, setEditingDeck] = useState<Deck | null>(null);
  const [deletingDeck, setDeletingDeck] = useState<Deck | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  const openCreateModal = () => {
    setEditingDeck(null);
    setFormOpen(true);
  };

  const openEditModal = (deck: Deck) => {
    setEditingDeck(deck);
    setFormOpen(true);
  };

  const handleFormSubmit = async (input: DeckFormInput) => {
    if (editingDeck) {
      await decksApi.updateDeck(editingDeck.id, input);
    } else {
      await decksApi.createDeck(input);
    }
    await queryClient.invalidateQueries({ queryKey: ["decks"] });
    setFormOpen(false);
  };

  const handleConfirmDelete = async () => {
    if (!deletingDeck) return;
    setIsDeleting(true);
    setDeleteError("");
    try {
      await decksApi.deleteDeck(deletingDeck.id);
      await queryClient.invalidateQueries({ queryKey: ["decks"] });
      setDeletingDeck(null);
    } catch (err: unknown) {
      const error = err as Error;
      setDeleteError(error.message || "Could not delete this deck.");
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />
        <main className="p-8 max-w-7xl w-full mx-auto flex flex-col gap-6">
          <div className="flex items-center justify-between">
            <h1 className="text-title-page font-bold text-lumora-primary">Decks</h1>
            <button
              type="button"
              onClick={openCreateModal}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 transition-opacity"
            >
              <Plus className="w-4 h-4 stroke-[2]" />
              <span>Create Deck</span>
            </button>
          </div>

          {isError && <ErrorBanner message={error?.message || "Could not load decks."} />}

          {isLoading && (
            <p className="text-body-default text-lumora-secondary">Loading decks...</p>
          )}

          {!isLoading && !isError && decks && decks.length === 0 && (
            <EmptyState
              icon={Folder}
              message="No decks yet. Create your first deck to start reviewing."
              actionLabel="+ Create Deck"
              onAction={openCreateModal}
            />
          )}

          {!isLoading && decks && decks.length > 0 && (
            // Backend GET /api/decks returns the full list with no pagination
            // params (see DeckController.getMyDecks) -- so Day 2 renders the
            // whole list at once instead of faking client-side pagination.
            // Real pagination/infinite scroll needs offset+limit support added
            // to that endpoint first.
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[14px]">
              {decks.map((deck) => (
                <DeckGridItem
                  key={deck.id}
                  deck={deck}
                  onEdit={() => openEditModal(deck)}
                  onDelete={() => setDeletingDeck(deck)}
                />
              ))}
            </div>
          )}
        </main>
      </div>

      <DeckFormModal
        open={formOpen}
        deck={editingDeck}
        onClose={() => setFormOpen(false)}
        onSubmit={handleFormSubmit}
      />

      <ConfirmDialog
        open={!!deletingDeck}
        title="Delete deck?"
        description={
          deleteError ||
          `This will permanently delete "${deletingDeck?.name}" and all of its cards. This cannot be undone.`
        }
        confirmLabel="Delete"
        danger
        isLoading={isDeleting}
        onConfirm={handleConfirmDelete}
        onCancel={() => {
          setDeletingDeck(null);
          setDeleteError("");
        }}
      />
    </div>
  );
}
