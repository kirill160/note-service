CREATE TABLE IF NOT exists note_tags
(
    note_id BIGINT REFERENCES
    notes(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY(note_id, tag_id)
);
