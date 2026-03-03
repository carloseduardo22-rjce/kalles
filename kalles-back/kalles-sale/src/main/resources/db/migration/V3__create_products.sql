CREATE TABLE product (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150)    NOT NULL,
    internal_code   VARCHAR(50)     NOT NULL UNIQUE,
    barcode         VARCHAR(50)     UNIQUE,
    description     TEXT,
    price           NUMERIC(19, 2)  NOT NULL,
    active          BOOLEAN         NOT NULL
);

CREATE INDEX idx_product_internal_code ON product (internal_code);
CREATE INDEX idx_product_barcode       ON product (barcode);
