ALTER TABLE client RENAME COLUMN cpf TO document;
UPDATE client SET document = regexp_replace(document, '[^0-9]', '', 'g');
ALTER TABLE client ALTER COLUMN document SET NOT NULL;
ALTER TABLE client ADD COLUMN person_type VARCHAR(8) NOT NULL DEFAULT 'FISICA';
ALTER TABLE client ADD CONSTRAINT ck_client_person_type CHECK (person_type IN ('FISICA', 'JURIDICA'));
ALTER TABLE client ADD COLUMN trade_name VARCHAR(100);
ALTER TABLE client ADD COLUMN state_registration VARCHAR(14);
ALTER TABLE client RENAME CONSTRAINT uk_client_cpf_company TO uk_client_document_company;
ALTER INDEX idx_client_cpf RENAME TO idx_client_document;
