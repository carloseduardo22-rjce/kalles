ALTER TABLE account 
ADD COLUMN company_id UUID NULL;

-- An administrator might not belong to a single company, 
-- but operators must. This ties an operator to their physical store location.
COMMENT ON COLUMN account.company_id IS 'If set, the account is restricted to a specific company (e.g. operators)';
