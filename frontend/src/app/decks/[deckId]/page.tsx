"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus, Pencil, GraduationCap, BookOpen } from "lucide-react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { CardRow } from "@/components/decks/CardRow";
import { DeckFormModal } from "@/components/decks/DeckFormModal";
import { CardFormModal } from "@/components/decks/CardFormModal";
import { EmptyState } from "@/components/shared/EmptyState";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { ErrorBanner } from "@/components/shared/ErrorBanner";
import { isAuthenticated } from "@/lib/auth";
import * as decksApi from "@/lib/decks";
import * as cardsApi from "@/lib/cards";
import { Deck, DeckFormInput } from "@/types/deck";
import { Card, CardFormInput } from "@/types/card";

export default function DeckDetailPage() {
  const params = useParams();
  const router = useRouter();
  const queryClient = useQueryClient();
  const deckId = Number(params?.deckId);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
    }
  }, [router]);

  const {
    data: deck,
    isLoading: isDeckLoading,
    isError: isDeckError,
    error: deckError,
  } = useQuery<Deck, Error>({
    queryKey: ["deck", deckId],
    queryFn: () => decksApi.getDeck(deckId),
    enabled: Number.isFinite(deckId),
  });

  const {
    data: cards,
    isLoading: areCardsLoading,
    isError: isCardsError,
    error: cardsError,
  } = useQuery<Card[], Error>({
    queryKey: ["cards", deckId],
    queryFn: () => cardsApi.getCardsByDeck(deckId),
    enabled: Number.isFinite(deckId),
  });

  const [isEditDeckOpen, setIsEditDeckOpen] = useState(false);
  const [cardFormOpen, setCardFormOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<Card | null>(null);
  const [deletingCard, setDeletingCard] = useState<Card | null>(null);
  const [isDeletingCard, setIsDeletingCard] = useState(false);
  const [deleteCardError, setDeleteCardError] = useState("");

  const handleEditDeckSubmit = async (input: DeckFormInput) => {
    await decksApi.updateDeck(deckId, input);
    await queryClient.invalidateQueries({ queryKey: ["deck", deckId] });
    await queryClient.invalidateQueries({ queryKey: ["decks"] });
    setIsEditDeckOpen(false);
  };

  const openCreateCardModal = () => {
    setEditingCard(null);
    setCardFormOpen(true);
  };

  const openEditCardModal = (card: Card) => {
    setEditingCard(card);
    setCardFormOpen(true);
  };

  const handleCardFormSubmit = async (input: CardFormInput) => {
    if (editingCard) {
      await cardsApi.updateCard(editingCard.id, input);
    } else {
      await cardsApi.createCard(deckId, input);
    }
    await queryClient.invalidateQueries({ queryKey: ["cards", deckId] });
    setCardFormOpen(false);
  };

  const handleConfirmDeleteCard = async () => {
    if (!deletingCard) return;
    setIsDeletingCard(true);
    setDeleteCardError("");
    try {
      await cardsApi.deleteCard(deletingCard.id);
      await queryClient.invalidateQueries({ queryKey: ["cards", deckId] });
      setDeletingCard(null);
    } catch (err: unknown) {
      const error = err as Error;
      setDeleteCardError(error.message || "Could not delete this card.");
    } finally {
      setIsDeletingCard(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-lumora-bg text-lumora-primary">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar />
        <main className="p-8 max-w-4xl w-full mx-auto flex flex-col gap-6">
          {isDeckLoading && (
            <p className="text-body-default text-lumora-secondary">Loading deck...</p>
          )}

          {isDeckError && (
            <ErrorBanner message={deckError?.message || "Could not load this deck."} />
          )}

          {deck && (
            <>
              <div className="flex items-start justify-between gap-4 flex-wrap">
                <div>
                  <h1 className="text-title-page font-bold text-lumora-primary">{deck.name}</h1>
                  {deck.description && (
                    <p className="text-body-default text-lumora-secondary mt-1">
                      {deck.description}
                    </p>
                  )}
                </div>
                <div className="flex items-center gap-3 shrink-0">
                  <button
                    type="button"
                    onClick={() => setIsEditDeckOpen(true)}
                    className="inline-flex items-center gap-2 px-4 py-2 rounded-btn border border-lumora-border bg-lumora-surface text-lumora-primary hover:bg-lumora-surface-hover transition-colors font-semibold text-body-default"
                  >
                    <Pencil className="w-4 h-4 stroke-[1.8]" />
                    <span>Edit Deck</span>
                  </button>
                  <Link
                    href={`/study/${deck.id}`}
                    className="inline-flex items-center gap-2 px-4 py-2 rounded-btn bg-lumora-btn text-lumora-btn-text text-body-default font-semibold hover:opacity-90 transition-opacity"
                  >
                    <GraduationCap className="w-4 h-4 stroke-[1.8]" />
                    <span>Study This Deck</span>
                  </Link>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <h2 className="text-card-heading font-semibold text-lumora-primary">Cards</h2>
                <button
                  type="button"
                  onClick={openCreateCardModal}
                  className="inline-flex items-center gap-1.5 text-body-default font-semibold text-lumora-secondary hover:text-lumora-primary transition-colors"
                >
                  <Plus className="w-4 h-4 stroke-[2]" />
                  <span>Add Card</span>
                </button>
              </div>

              {isCardsError && (
                <ErrorBanner message={cardsError?.message || "Could not load cards."} />
              )}

              {areCardsLoading && (
                <p className="text-body-default text-lumora-secondary">Loading cards...</p>
              )}

              {!areCardsLoading && !isCardsError && cards && cards.length === 0 && (
                <EmptyState
                  icon={BookOpen}
                  message="This deck has no cards yet."
                  actionLabel="+ Add Card"
                  onAction={openCreateCardModal}
                />
              )}

              {!areCardsLoading && cards && cards.length > 0 && (
                <div className="bg-lumora-surface border border-lumora-border rounded-card overflow-hidden">
                  {cards.map((card) => (
                    <CardRow
                      key={card.id}
                      card={card}
                      onEdit={() => openEditCardModal(card)}
                      onDelete={() => setDeletingCard(card)}
                    />
                  ))}
                </div>
              )}
            </>
          )}
        </main>
      </div>

      {deck && (
        <DeckFormModal
          open={isEditDeckOpen}
          deck={deck}
          onClose={() => setIsEditDeckOpen(false)}
          onSubmit={handleEditDeckSubmit}
        />
      )}

      <CardFormModal
        open={cardFormOpen}
        card={editingCard}
        onClose={() => setCardFormOpen(false)}
        onSubmit={handleCardFormSubmit}
      />

      <ConfirmDialog
        open={!!deletingCard}
        title="Delete card?"
        description={
          deleteCardError || "This will permanently delete this card. This cannot be undone."
        }
        confirmLabel="Delete"
        danger
        isLoading={isDeletingCard}
        onConfirm={handleConfirmDeleteCard}
        onCancel={() => {
          setDeletingCard(null);
          setDeleteCardError("");
        }}
      />
    </div>
  );
}
