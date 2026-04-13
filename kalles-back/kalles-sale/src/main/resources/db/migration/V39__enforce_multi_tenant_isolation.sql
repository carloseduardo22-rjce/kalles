-- =============================================================================
-- V39: Enforce multi-tenant isolation across all domains
-- =============================================================================

-- 1. Product: tenant_id NOT NULL + tenant-scoped unique constraints
UPDATE product SET tenant_id = (SELECT id FROM tenant LIMIT 1) WHERE tenant_id IS NULL;
ALTER TABLE product ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE product DROP CONSTRAINT IF EXISTS product_internal_code_key;
ALTER TABLE product DROP CONSTRAINT IF EXISTS product_barcode_key;
DROP INDEX IF EXISTS idx_product_internal_code;
DROP INDEX IF EXISTS idx_product_barcode;
ALTER TABLE product ADD CONSTRAINT uk_product_internal_code_tenant UNIQUE (internal_code, tenant_id);
ALTER TABLE product ADD CONSTRAINT uk_product_barcode_tenant UNIQUE (barcode, tenant_id);

-- 2. Warehouse: company_id NOT NULL
UPDATE warehouse SET company_id = (SELECT id FROM company LIMIT 1) WHERE company_id IS NULL;
ALTER TABLE warehouse ALTER COLUMN company_id SET NOT NULL;

-- 3. Client: add company_id, company-scoped CPF unique
ALTER TABLE client ADD COLUMN IF NOT EXISTS company_id UUID;
UPDATE client SET company_id = (SELECT id FROM company LIMIT 1) WHERE company_id IS NULL;
ALTER TABLE client ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE client ADD CONSTRAINT fk_client_company FOREIGN KEY (company_id) REFERENCES company(id);
CREATE INDEX IF NOT EXISTS idx_client_company_id ON client(company_id);
ALTER TABLE client DROP CONSTRAINT IF EXISTS client_cpf_key;
ALTER TABLE client ADD CONSTRAINT uk_client_cpf_company UNIQUE (cpf, company_id);

-- 4. Operator: add company_id, company-scoped code unique
ALTER TABLE operators ADD COLUMN IF NOT EXISTS company_id UUID;
UPDATE operators SET company_id = (SELECT id FROM company LIMIT 1) WHERE company_id IS NULL;
ALTER TABLE operators ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE operators ADD CONSTRAINT fk_operator_company FOREIGN KEY (company_id) REFERENCES company(id);
CREATE INDEX IF NOT EXISTS idx_operator_company_id ON operators(company_id);
ALTER TABLE operators DROP CONSTRAINT IF EXISTS operators_code_key;
ALTER TABLE operators ADD CONSTRAINT uk_operator_code_company UNIQUE (code, company_id);

-- 5. Cash register: company-scoped code unique, company_id NOT NULL
ALTER TABLE cash_registers DROP CONSTRAINT IF EXISTS cash_registers_code_key;
UPDATE cash_registers SET company_id = (SELECT id FROM company LIMIT 1) WHERE company_id IS NULL;
ALTER TABLE cash_registers ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE cash_registers ADD CONSTRAINT uk_cash_register_code_company UNIQUE (code, company_id);

-- 6. Fidelity policy: company_id NOT NULL
UPDATE fidelity_policy SET company_id = (SELECT id FROM company LIMIT 1) WHERE company_id IS NULL;
ALTER TABLE fidelity_policy ALTER COLUMN company_id SET NOT NULL;

-- 7. Goals: company_id NOT NULL
UPDATE goals SET company_id = (SELECT id FROM company LIMIT 1) WHERE company_id IS NULL;
ALTER TABLE goals ALTER COLUMN company_id SET NOT NULL;

-- 8. Account: allow the same email in different tenants, but never in the same tenant
ALTER TABLE account DROP CONSTRAINT IF EXISTS account_email_key;
ALTER TABLE account DROP CONSTRAINT IF EXISTS uk_account_email_tenant;
DROP INDEX IF EXISTS idx_account_email;
CREATE INDEX IF NOT EXISTS idx_account_email ON account(email);
ALTER TABLE account ADD CONSTRAINT uk_account_email_tenant UNIQUE (email, tenant_id);

-- 9. Support: tenant/company scope for helpdesk data
ALTER TABLE support.users ADD COLUMN IF NOT EXISTS tenant_id UUID;
ALTER TABLE support.agents ADD COLUMN IF NOT EXISTS tenant_id UUID;
ALTER TABLE support.categories ADD COLUMN IF NOT EXISTS tenant_id UUID;
ALTER TABLE support.tickets ADD COLUMN IF NOT EXISTS tenant_id UUID;
ALTER TABLE support.tickets ADD COLUMN IF NOT EXISTS company_id UUID;
ALTER TABLE support.interactions ADD COLUMN IF NOT EXISTS tenant_id UUID;

UPDATE support.users SET tenant_id = (SELECT id FROM tenant LIMIT 1) WHERE tenant_id IS NULL;
UPDATE support.agents SET tenant_id = (SELECT id FROM tenant LIMIT 1) WHERE tenant_id IS NULL;
UPDATE support.categories SET tenant_id = (SELECT id FROM tenant LIMIT 1) WHERE tenant_id IS NULL;
UPDATE support.tickets SET tenant_id = (SELECT id FROM tenant LIMIT 1) WHERE tenant_id IS NULL;
UPDATE support.tickets SET company_id = (SELECT id FROM company LIMIT 1) WHERE company_id IS NULL;
UPDATE support.interactions i
SET tenant_id = t.tenant_id
FROM support.tickets t
WHERE i.ticket_id = t.id
  AND i.tenant_id IS NULL;

ALTER TABLE support.users ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE support.agents ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE support.categories ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE support.tickets ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE support.interactions ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE support.interactions ALTER COLUMN tenant_id SET DEFAULT '123e4567-e89b-12d3-a456-426614174000'::uuid;

ALTER TABLE support.users DROP CONSTRAINT IF EXISTS uq_users_email;
ALTER TABLE support.agents DROP CONSTRAINT IF EXISTS uq_agents_employee_id;
ALTER TABLE support.categories DROP CONSTRAINT IF EXISTS uq_categories_name_subcategory;
ALTER TABLE support.users ADD CONSTRAINT uq_support_users_email_tenant UNIQUE (email, tenant_id);
ALTER TABLE support.agents ADD CONSTRAINT uq_support_agents_employee_tenant UNIQUE (employee_id, tenant_id);
ALTER TABLE support.categories ADD CONSTRAINT uq_support_categories_name_subcategory_tenant UNIQUE (name, subcategory, tenant_id);

ALTER TABLE support.users ADD CONSTRAINT fk_support_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id);
ALTER TABLE support.agents ADD CONSTRAINT fk_support_agents_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id);
ALTER TABLE support.categories ADD CONSTRAINT fk_support_categories_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id);
ALTER TABLE support.tickets ADD CONSTRAINT fk_support_tickets_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id);
ALTER TABLE support.tickets ADD CONSTRAINT fk_support_tickets_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE support.interactions ADD CONSTRAINT fk_support_interactions_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id);

CREATE INDEX IF NOT EXISTS idx_support_tickets_tenant_id ON support.tickets(tenant_id);
CREATE INDEX IF NOT EXISTS idx_support_tickets_company_id ON support.tickets(company_id);
CREATE INDEX IF NOT EXISTS idx_support_interactions_tenant_id ON support.interactions(tenant_id);
