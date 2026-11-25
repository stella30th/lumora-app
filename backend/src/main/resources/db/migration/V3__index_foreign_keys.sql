CREATE INDEX IF NOT EXISTS idx_decks_owner_id ON decks (owner_id);
CREATE INDEX IF NOT EXISTS idx_cards_deck_id ON cards (deck_id);
CREATE INDEX IF NOT EXISTS idx_card_progress_card_id ON card_progress (card_id);
CREATE INDEX IF NOT EXISTS idx_review_log_card_id ON review_log (card_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);
