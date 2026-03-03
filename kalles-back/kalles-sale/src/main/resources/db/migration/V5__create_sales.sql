CREATE TABLE sale (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT,
    session_token   VARCHAR(255)    NOT NULL,
    state           VARCHAR(255)    NOT NULL,
    subtotal        NUMERIC(19, 2)  NOT NULL DEFAULT 0,
    total           NUMERIC(19, 2)  NOT NULL DEFAULT 0,
    amount_due      NUMERIC(19, 2)  NOT NULL DEFAULT 0
);

CREATE INDEX idx_sale_session_token ON sale (session_token);
CREATE INDEX idx_sale_state         ON sale (state);
