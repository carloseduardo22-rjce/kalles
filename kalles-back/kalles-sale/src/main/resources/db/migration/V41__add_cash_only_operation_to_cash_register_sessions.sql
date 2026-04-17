ALTER TABLE cash_register_sessions
    ADD COLUMN cash_only_operation BOOLEAN NOT NULL DEFAULT FALSE;
