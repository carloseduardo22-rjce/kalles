CREATE TABLE fiscal_configurations (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL,
    model VARCHAR(20) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    state_code VARCHAR(2) NOT NULL,
    csc_id VARCHAR(20),
    csc_token VARCHAR(200),
    series INTEGER NOT NULL DEFAULT 1,
    next_number BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT uk_fiscal_config_company_model_env UNIQUE (tenant_id, company_id, model, environment)
);

CREATE TABLE fiscal_certificates (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL,
    protected_content TEXT,
    protected_password TEXT
);

CREATE TABLE fiscal_documents (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL,
    sale_id UUID NOT NULL,
    model VARCHAR(20) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    access_key VARCHAR(60),
    authorization_protocol VARCHAR(80),
    rejection_reason VARCHAR(500),
    authorized_xml TEXT,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_fiscal_documents_tenant_company_sale
    ON fiscal_documents (tenant_id, company_id, sale_id);

CREATE INDEX idx_fiscal_certificates_tenant_company_active
    ON fiscal_certificates (tenant_id, company_id, active);
