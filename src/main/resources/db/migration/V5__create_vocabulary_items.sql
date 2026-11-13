CREATE TABLE vocabulary_items
(
    id         BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT       NOT NULL,
    term       VARCHAR(255) NOT NULL,
    definition TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_vocabulary_items_chapter
        FOREIGN KEY (chapter_id)
            REFERENCES chapters (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_vocabulary_items_chapter_id
    ON vocabulary_items (chapter_id);
