CREATE TABLE fiscal_issuer_profiles (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    legal_name VARCHAR(160) NOT NULL,
    trade_name VARCHAR(160),
    state_registration VARCHAR(20) NOT NULL,
    tax_regime VARCHAR(30) NOT NULL,
    cnae VARCHAR(10),
    CONSTRAINT uk_fiscal_issuer_profile_company UNIQUE (tenant_id, company_id)
);

CREATE TABLE fiscal_issuer_addresses (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL,
    zip_code VARCHAR(8) NOT NULL,
    state_code VARCHAR(2) NOT NULL,
    state_ibge_code INTEGER NOT NULL,
    city_name VARCHAR(80) NOT NULL,
    city_ibge_code INTEGER NOT NULL,
    district VARCHAR(80) NOT NULL,
    street VARCHAR(120) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(80),
    country_name VARCHAR(60) NOT NULL,
    country_code INTEGER NOT NULL,
    CONSTRAINT uk_fiscal_issuer_address_company UNIQUE (tenant_id, company_id)
);

ALTER TABLE fiscal_product_classifications ADD COLUMN IF NOT EXISTS cfop_sale VARCHAR(4);
ALTER TABLE fiscal_product_classifications ADD COLUMN IF NOT EXISTS origin VARCHAR(1);
ALTER TABLE fiscal_product_classifications ADD COLUMN IF NOT EXISTS csosn VARCHAR(4);
ALTER TABLE fiscal_product_classifications ADD COLUMN IF NOT EXISTS cst VARCHAR(3);
ALTER TABLE fiscal_product_classifications ADD COLUMN IF NOT EXISTS unit VARCHAR(6);
ALTER TABLE fiscal_product_classifications ADD COLUMN IF NOT EXISTS gtin VARCHAR(14);
