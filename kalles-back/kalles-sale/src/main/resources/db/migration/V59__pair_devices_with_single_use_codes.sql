DELETE FROM pos_device_session;
ALTER TABLE pos_device_session DROP COLUMN token;
ALTER TABLE pos_device_session DROP COLUMN expires_at;
ALTER TABLE pos_device_session ADD COLUMN token_hash VARCHAR(64) NOT NULL;
ALTER TABLE pos_device_session ADD COLUMN revoked_at TIMESTAMP;
ALTER TABLE pos_device_session ADD CONSTRAINT uk_pos_device_session_token_hash UNIQUE (token_hash);
ALTER TABLE pos_device_session ADD CONSTRAINT fk_pos_device_session_cash_register
    FOREIGN KEY (pos_id) REFERENCES cash_registers (id);
DROP INDEX IF EXISTS idx_pos_device_session_token;
CREATE UNIQUE INDEX uk_pos_device_session_active_pos ON pos_device_session (pos_id) WHERE active;

CREATE TABLE pos_pairing_code (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id       UUID        NOT NULL REFERENCES company (id),
    cash_register_id UUID        NOT NULL REFERENCES cash_registers (id),
    code_hash        VARCHAR(64) NOT NULL,
    expires_at       TIMESTAMP   NOT NULL,
    used_at          TIMESTAMP,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pos_pairing_code_hash UNIQUE (code_hash)
);

ALTER TABLE refresh_token_session DROP COLUMN pos_id;
