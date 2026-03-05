CREATE TABLE stock (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    version     BIGINT  NOT NULL DEFAULT 0,
    product_id  UUID    NOT NULL REFERENCES product (id),
    location_id UUID    NOT NULL REFERENCES location (id),
    quantity    INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_product_location UNIQUE (product_id, location_id)
);

CREATE INDEX idx_stock_product_id  ON stock (product_id);
CREATE INDEX idx_stock_location_id ON stock (location_id);
