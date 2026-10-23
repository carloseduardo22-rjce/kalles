DO $$
DECLARE
    missing    INTEGER;
    duplicated INTEGER;
BEGIN
    SELECT count(*) INTO missing
    FROM client
    WHERE coalesce(regexp_replace(cpf, '[^0-9]', '', 'g'), '') = '';

    SELECT count(*) INTO duplicated
    FROM (
        SELECT 1
        FROM client
        WHERE cpf IS NOT NULL
        GROUP BY company_id, regexp_replace(cpf, '[^0-9]', '', 'g')
        HAVING count(*) > 1
    ) d;

    IF missing > 0 OR duplicated > 0 THEN
        RAISE EXCEPTION 'V58: % client(s) without CPF and % CPF(s) duplicated per company once punctuation is removed; fix them before migrating', missing, duplicated;
    END IF;
END $$;

ALTER TABLE client RENAME COLUMN cpf TO document;
UPDATE client SET document = regexp_replace(document, '[^0-9]', '', 'g');
ALTER TABLE client ALTER COLUMN document SET NOT NULL;
ALTER TABLE client ADD COLUMN person_type VARCHAR(8) NOT NULL DEFAULT 'FISICA';
ALTER TABLE client ADD CONSTRAINT ck_client_person_type CHECK (person_type IN ('FISICA', 'JURIDICA'));
ALTER TABLE client ADD COLUMN trade_name VARCHAR(100);
ALTER TABLE client ADD COLUMN state_registration VARCHAR(14);
ALTER TABLE client RENAME CONSTRAINT uk_client_cpf_company TO uk_client_document_company;
ALTER INDEX idx_client_cpf RENAME TO idx_client_document;
