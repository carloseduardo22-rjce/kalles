-- A venda não tinha timestamps próprios: relatórios atribuíam todas as vendas
-- à data de ABERTURA da sessão de caixa (sessão virando meia-noite = faturamento
-- no dia errado). Além disso, a auditoria passa a registrar descontos de item.

ALTER TABLE sale ADD COLUMN created_at   TIMESTAMP;
ALTER TABLE sale ADD COLUMN updated_at   TIMESTAMP;
ALTER TABLE sale ADD COLUMN completed_at TIMESTAMP;

-- Backfill: melhor aproximação disponível para vendas antigas é a abertura da sessão.
UPDATE sale s
SET created_at = crs.opened_at
FROM cash_register_sessions crs
WHERE CAST(crs.id AS TEXT) = s.session_token
  AND s.created_at IS NULL;

UPDATE sale s
SET completed_at = crs.opened_at
FROM cash_register_sessions crs
WHERE CAST(crs.id AS TEXT) = s.session_token
  AND s.state = 'COMPLETED'
  AND s.completed_at IS NULL;

CREATE INDEX idx_sale_completed_at ON sale (completed_at);

-- Auditoria de desconto em item (valor aplicado)
ALTER TABLE sale_audit_events ADD COLUMN discount_amount NUMERIC(19, 2);
