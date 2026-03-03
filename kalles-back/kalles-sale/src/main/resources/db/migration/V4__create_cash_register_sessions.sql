CREATE TABLE cash_register_sessions (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    cash_register_id UUID           NOT NULL REFERENCES cash_registers (id),
    operator_id     UUID            NOT NULL REFERENCES operators (id),
    initial_amount  NUMERIC(19, 2)  NOT NULL,
    opened_at       TIMESTAMP,
    closed_at       TIMESTAMP,
    status          VARCHAR(50)     NOT NULL
);
