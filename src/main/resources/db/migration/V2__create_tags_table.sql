CREATE TABLE IF NOT exists tags
(
    id bigserial PRIMARY key,
    name VARCHAR(50) unique not null
);
