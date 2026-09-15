ALTER TABLE notes
    ADD COLUMN IF NOT EXISTS  search_vector tsvector GENERATED ALWAYS AS(
    setweight(to_tsvector('russian', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('russian', coalesce(content, '')), 'B')
) STORED;