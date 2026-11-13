ALTER TABLE books
ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE books
ADD CONSTRAINT fk_books_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE CASCADE;

CREATE INDEX idx_books_user_id ON books (user_id);
