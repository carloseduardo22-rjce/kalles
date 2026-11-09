ALTER TABLE operators ADD COLUMN account_id UUID REFERENCES account (id);
ALTER TABLE operators ADD CONSTRAINT uk_operator_account_company UNIQUE (account_id, company_id);
