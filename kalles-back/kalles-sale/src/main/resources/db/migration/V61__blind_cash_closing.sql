ALTER TABLE company
    ADD COLUMN cash_closing_tolerance NUMERIC(19, 2) NOT NULL DEFAULT 0.00
        CONSTRAINT ck_company_cash_closing_tolerance CHECK (cash_closing_tolerance >= 0);

ALTER TABLE cash_register_closings
    ALTER COLUMN authorized_by_operator_id DROP NOT NULL;

ALTER TABLE cash_register_sessions
    ADD COLUMN first_divergent_counted_amount NUMERIC(19, 2);
