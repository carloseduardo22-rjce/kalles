-- Add company_id to sale table
ALTER TABLE sale ADD COLUMN company_id UUID;
CREATE INDEX idx_sale_company_id ON sale(company_id);

-- Add company_id to cash_registers table
ALTER TABLE cash_registers ADD COLUMN company_id UUID;
CREATE INDEX idx_cash_registers_company_id ON cash_registers(company_id);

-- Add company_id to warehouse table
ALTER TABLE warehouse ADD COLUMN company_id UUID;
CREATE INDEX idx_warehouse_company_id ON warehouse(company_id);
