CREATE TABLE stock_adjustments (
    id                UUID         NOT NULL,
    version           BIGINT,
    company_id        UUID         NOT NULL,
    product_id        UUID         NOT NULL,
    location_id       UUID         NOT NULL,
    previous_quantity INTEGER      NOT NULL,
    new_quantity      INTEGER      NOT NULL,
    reason            VARCHAR(200) NOT NULL,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT pk_stock_adjustments PRIMARY KEY (id),
    CONSTRAINT fk_stock_adjustments_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_stock_adjustments_location FOREIGN KEY (location_id) REFERENCES location (id)
);

CREATE INDEX idx_stock_adjustments_company_id ON stock_adjustments (company_id);
CREATE INDEX idx_stock_adjustments_created_at ON stock_adjustments (created_at);
CREATE INDEX idx_stock_adjustments_product_id ON stock_adjustments (product_id);
