CREATE TABLE payment (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id         UUID            NOT NULL REFERENCES sale (id),
    method          VARCHAR(50)     NOT NULL,
    amount          NUMERIC(19, 2)  NOT NULL,
    change_amount   NUMERIC(19, 2)  NOT NULL DEFAULT 0,
    transaction_id  VARCHAR(100),
    confirmed       BOOLEAN         NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_payment_sale_id        ON payment (sale_id);
CREATE INDEX idx_payment_transaction_id ON payment (transaction_id);
