CREATE TABLE fiscal_product_classifications (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL,
    product_id UUID NOT NULL,
    ncm VARCHAR(8) NOT NULL,
    cest VARCHAR(10),
    cfop VARCHAR(4),
    CONSTRAINT uk_fiscal_product_classification_scope UNIQUE (tenant_id, company_id, product_id)
);

CREATE INDEX idx_fiscal_product_classifications_product
    ON fiscal_product_classifications (tenant_id, company_id, product_id);
