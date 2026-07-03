-- Garante no máximo UMA venda ativa por sessão de caixa.
--
-- Contexto: requisições concorrentes em getOrCreateSale podiam criar duas
-- vendas OPEN para a mesma sessão. A partir daí, as consultas que esperam
-- resultado único (findBySessionTokenAndStateIn via Optional) passavam a
-- falhar com IncorrectResultSizeDataAccessException, travando o PDV.

-- 1) Saneia duplicatas pré-existentes: mantém uma venda ativa por sessão
--    e cancela as demais. Sem timestamp na tabela, o desempate usa ctid
--    (mantém a linha inserida mais recentemente).
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY session_token ORDER BY ctid DESC) AS rn
    FROM sale
    WHERE state IN ('OPEN', 'ON_HOLD', 'PAYMENT_IN_PROGRESS', 'PAID')
)
UPDATE sale
SET state = 'CANCELED'
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

-- 2) Índice único parcial: bloqueia nova venda ativa enquanto existir outra
--    não finalizada (COMPLETED/CANCELED ficam fora do índice).
CREATE UNIQUE INDEX uk_sale_active_per_session
    ON sale (session_token)
    WHERE state IN ('OPEN', 'ON_HOLD', 'PAYMENT_IN_PROGRESS', 'PAID');
