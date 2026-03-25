CREATE TABLE account (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_account_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id)
);

CREATE INDEX idx_account_email ON account(email);
CREATE INDEX idx_account_tenant ON account(tenant_id);
