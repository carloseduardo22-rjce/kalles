-- Add company_id to goals table
ALTER TABLE goals ADD COLUMN company_id UUID;
CREATE INDEX idx_goals_company_id ON goals(company_id);

-- Add company_id to fidelity_policy
ALTER TABLE fidelity_policy ADD COLUMN company_id UUID;
CREATE INDEX idx_fidelity_policy_company_id ON fidelity_policy(company_id);
