CREATE TABLE pos_device_session (
    id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    UUID            NOT NULL,
    pos_id        UUID            NOT NULL,
    token         VARCHAR(255)    NOT NULL UNIQUE,
    expires_at    TIMESTAMP       NOT NULL,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active        BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_pos_device_session_token ON pos_device_session(token);
