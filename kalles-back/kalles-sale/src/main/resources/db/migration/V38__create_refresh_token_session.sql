CREATE TABLE refresh_token_session (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    pos_id UUID NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_refresh_token_session_account
        FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_session_account ON refresh_token_session(account_id);
CREATE INDEX idx_refresh_token_session_expires_at ON refresh_token_session(expires_at);
