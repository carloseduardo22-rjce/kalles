CREATE TABLE sale_item (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id         UUID            NOT NULL REFERENCES sale (id),
    product_id      UUID            NOT NULL REFERENCES product (id),
    quantity        INTEGER         NOT NULL,
    unit_price      NUMERIC(19, 2)  NOT NULL,
    discount        NUMERIC(19, 2)  NOT NULL DEFAULT 0
);

CREATE INDEX idx_saleitem_sale_id    ON sale_item (sale_id);
CREATE INDEX idx_saleitem_product_id ON sale_item (product_id);
