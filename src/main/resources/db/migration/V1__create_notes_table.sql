CREATE TABLE IF NOT EXISTS notes
(
    id BIGSERIAL PRIMARY key,
    title VARCHAR(250) not null,
    content VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    archive BOOLEAN default false
);