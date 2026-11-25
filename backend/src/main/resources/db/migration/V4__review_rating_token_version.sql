ALTER TABLE review_log ADD COLUMN rating VARCHAR(10);
ALTER TABLE review_log ADD CONSTRAINT ck_review_log_rating
    CHECK (rating IS NULL OR rating IN ('AGAIN', 'HARD', 'GOOD', 'EASY'));

ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE refresh_tokens ADD COLUMN rotated_at TIMESTAMP(6);
