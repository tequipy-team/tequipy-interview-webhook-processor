CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
-- TODO: add indexes for performance later
