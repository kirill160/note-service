CREATE TABLE IF NOT EXISTS notes
(
    id BIGSERIAL PRIMARY key,
    title VARCHAR(250) not null,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    archive BOOLEAN default false
);

CREATE TABLE IF NOT exists tags
(
    id bigserial PRIMARY key,
    name VARCHAR(50) unique not null
);

CREATE TABLE IF NOT exists note_tags
(
    note_id BIGINT REFERENCES
    notes(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY(note_id, tag_id)
);

ALTER TABLE notes
ADD COLUMN IF NOT EXISTS  search_vector tsvector GENERATED ALWAYS AS(
    setweight(to_tsvector('russian', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('russian', coalesce(content, '')), 'B')
) STORED;

CREATE INDEX idx_notes_fts ON notes
USING GIN(search_vector);

