CREATE TABLE cash_register_closings (
    id                         UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id                 UUID            NOT NULL UNIQUE REFERENCES cash_register_sessions (id),
    authorized_by_operator_id  UUID            NOT NULL REFERENCES operators (id),
    completed_sales_count      INTEGER         NOT NULL,
    canceled_sales_count       INTEGER         NOT NULL,
    total_sold_amount          NUMERIC(19, 2)  NOT NULL,
    cash_sales_amount          NUMERIC(19, 2)  NOT NULL,
    expected_cash_amount       NUMERIC(19, 2)  NOT NULL,
    counted_cash_amount        NUMERIC(19, 2)  NOT NULL,
    cash_difference_amount     NUMERIC(19, 2)  NOT NULL
);

CREATE TABLE cash_register_closing_payment_totals (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    closing_id      UUID            NOT NULL REFERENCES cash_register_closings (id) ON DELETE CASCADE,
    payment_method  VARCHAR(50)     NOT NULL,
    amount          NUMERIC(19, 2)  NOT NULL
);

CREATE INDEX idx_cash_register_closings_session_id
    ON cash_register_closings (session_id);

CREATE INDEX idx_cash_register_closing_payment_totals_closing_id
    ON cash_register_closing_payment_totals (closing_id);
