WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY cash_register_id ORDER BY opened_at DESC NULLS LAST, ctid DESC) AS rn
    FROM cash_register_sessions
    WHERE status = 'OPEN'
)
UPDATE cash_register_sessions
SET status = 'CLOSED',
    closed_at = COALESCE(closed_at, now())
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY operator_id ORDER BY opened_at DESC NULLS LAST, ctid DESC) AS rn
    FROM cash_register_sessions
    WHERE status = 'OPEN'
)
UPDATE cash_register_sessions
SET status = 'CLOSED',
    closed_at = COALESCE(closed_at, now())
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

CREATE UNIQUE INDEX uk_session_open_per_cash_register
    ON cash_register_sessions (cash_register_id)
    WHERE status = 'OPEN';

CREATE UNIQUE INDEX uk_session_open_per_operator
    ON cash_register_sessions (operator_id)
    WHERE status = 'OPEN';
