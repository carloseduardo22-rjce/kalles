ALTER TABLE fiscal_configurations
    ADD COLUMN IF NOT EXISTS series INTEGER NOT NULL DEFAULT 1;

ALTER TABLE fiscal_configurations
    ADD COLUMN IF NOT EXISTS next_number BIGINT NOT NULL DEFAULT 1;

ALTER TABLE fiscal_certificates
    ADD COLUMN IF NOT EXISTS protected_content TEXT;

ALTER TABLE fiscal_certificates
    ADD COLUMN IF NOT EXISTS protected_password TEXT;

ALTER TABLE fiscal_documents
    ADD COLUMN IF NOT EXISTS authorized_xml TEXT;
