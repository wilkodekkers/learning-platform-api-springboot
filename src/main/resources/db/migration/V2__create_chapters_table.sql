CREATE TABLE chapters
(
    id         BIGSERIAL PRIMARY KEY,
    book_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    position   INTEGER      NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_chapters_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_chapters_book_position
        UNIQUE (book_id, position)
);
