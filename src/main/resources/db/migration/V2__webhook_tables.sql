CREATE TABLE webhook_events (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    state VARCHAR(20) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    rt TIMESTAMP NOT NULL
);

CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    status VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP NOT NULL
);
