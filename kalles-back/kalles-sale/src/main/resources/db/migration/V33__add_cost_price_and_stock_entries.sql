ALTER TABLE company_product
    ADD COLUMN cost_price NUMERIC(19, 2);

UPDATE company_product
SET cost_price = COALESCE(price, 0.01)
WHERE cost_price IS NULL;

ALTER TABLE company_product
    ALTER COLUMN cost_price SET NOT NULL;

CREATE TABLE stock_entries (
    id UUID PRIMARY KEY,
    version BIGINT,
    company_id UUID NOT NULL,
    product_id UUID NOT NULL,
    location_id UUID NOT NULL,
    quantity_added INTEGER NOT NULL,
    unit_cost NUMERIC(19, 2) NOT NULL,
    total_cost NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_stock_entries_company_id ON stock_entries(company_id);
CREATE INDEX idx_stock_entries_created_at ON stock_entries(created_at);
CREATE INDEX idx_stock_entries_product_id ON stock_entries(product_id);

ALTER TABLE stock_entries
    ADD CONSTRAINT fk_stock_entries_product
        FOREIGN KEY (product_id) REFERENCES product(id);

ALTER TABLE stock_entries
    ADD CONSTRAINT fk_stock_entries_location
        FOREIGN KEY (location_id) REFERENCES location(id);
