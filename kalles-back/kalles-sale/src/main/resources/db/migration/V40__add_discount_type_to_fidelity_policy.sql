ALTER TABLE fidelity_policy
    ADD COLUMN discount_type VARCHAR(20) NOT NULL DEFAULT 'FIXED';
