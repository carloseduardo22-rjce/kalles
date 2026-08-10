DROP INDEX IF EXISTS idx_payment_transaction_id;

CREATE UNIQUE INDEX uk_payment_transaction_id
    ON payment (transaction_id)
    WHERE transaction_id IS NOT NULL;
