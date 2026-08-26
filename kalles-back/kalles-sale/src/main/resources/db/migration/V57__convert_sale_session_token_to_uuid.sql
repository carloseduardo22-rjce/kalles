ALTER TABLE sale ADD COLUMN session_id UUID;

UPDATE sale
SET session_id = CAST(session_token AS UUID)
WHERE session_token ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

DO $$
DECLARE
    unresolved BIGINT;
BEGIN
    SELECT COUNT(*) INTO unresolved
    FROM sale s
    WHERE s.session_id IS NULL
       OR NOT EXISTS (SELECT 1 FROM cash_register_sessions crs WHERE crs.id = s.session_id);

    IF unresolved > 0 THEN
        RAISE EXCEPTION 'V57 interrompida: % venda(s) nao apontam para uma sessao de caixa existente', unresolved;
    END IF;
END $$;

ALTER TABLE sale ALTER COLUMN session_id SET NOT NULL;

ALTER TABLE sale ADD CONSTRAINT fk_sale_session
    FOREIGN KEY (session_id) REFERENCES cash_register_sessions (id);

ALTER TABLE sale DROP COLUMN session_token;

CREATE INDEX idx_sale_session_id ON sale (session_id);

CREATE UNIQUE INDEX uk_sale_active_per_session
    ON sale (session_id)
    WHERE state IN ('OPEN', 'PAYMENT_IN_PROGRESS', 'PAID');
