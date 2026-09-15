CREATE INDEX idx_notes_fts ON notes
    USING GIN(search_vector);
