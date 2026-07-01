CREATE TABLE payment_terminal_mappings (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    company_id UUID NOT NULL REFERENCES company(id),
    cash_register_id UUID NOT NULL REFERENCES cash_registers(id),
    provider VARCHAR(32) NOT NULL,
    terminal_serial VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_payment_terminal_mapping_active_cash_register_provider
    ON payment_terminal_mappings (cash_register_id, provider)
    WHERE active = TRUE;

CREATE UNIQUE INDEX uk_payment_terminal_mapping_active_serial_company_provider
    ON payment_terminal_mappings (company_id, provider, terminal_serial)
    WHERE active = TRUE;

CREATE INDEX idx_payment_terminal_mapping_tenant_company
    ON payment_terminal_mappings (tenant_id, company_id);
