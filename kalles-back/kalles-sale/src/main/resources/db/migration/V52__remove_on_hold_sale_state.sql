DROP INDEX IF EXISTS uk_sale_active_per_session;

UPDATE sale s
SET state = 'CANCELED'
WHERE s.state = 'ON_HOLD'
  AND EXISTS (
      SELECT 1
      FROM sale o
      WHERE o.session_token = s.session_token
        AND o.id <> s.id
        AND o.state IN ('OPEN', 'PAYMENT_IN_PROGRESS', 'PAID')
  );

UPDATE sale SET state = 'OPEN' WHERE state = 'ON_HOLD';

CREATE UNIQUE INDEX uk_sale_active_per_session
    ON sale (session_token)
    WHERE state IN ('OPEN', 'PAYMENT_IN_PROGRESS', 'PAID');
