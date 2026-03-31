-- 1. Add tenant_id to product table
ALTER TABLE product ADD COLUMN tenant_id UUID;

-- We don't drop price and active immediately without giving existing data a home,
-- but since this is an early stage DB, we might just drop them and let R__seed_data repopulate.
-- Alternatively, if we wanted to migrate existing products to a default company:
-- For this setup, we will just drop the columns as it's a major refactoring
ALTER TABLE product DROP COLUMN price;
ALTER TABLE product DROP COLUMN active;

-- 2. Create the Local Catalog table (company_product)
CREATE TABLE company_product (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID            NOT NULL,
    product_id  UUID            NOT NULL,
    price       NUMERIC(19, 2)  NOT NULL,
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_company_product_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
    -- Assuming we have a company table or mercadopago_company table, but we don't have a hard FK here
    -- since it might cross domain contexts. If we have a generic `company` table, we would add the FK.
    -- CONSTRAINT fk_cp_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE INDEX idx_company_product_company ON company_product(company_id);
CREATE INDEX idx_company_product_product ON company_product(product_id);
-- Ensure a product is only mapped once per company
ALTER TABLE company_product ADD CONSTRAINT unq_company_product UNIQUE (company_id, product_id);
