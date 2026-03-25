CREATE TABLE account_verification (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_acc_verification_acc FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);

CREATE INDEX idx_account_verification_code ON account_verification(code);
CREATE INDEX idx_account_verification_account ON account_verification(account_id);
