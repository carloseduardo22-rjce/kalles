ALTER TABLE account
    ADD CONSTRAINT fk_account_company
        FOREIGN KEY (company_id) REFERENCES company(id);

CREATE INDEX idx_account_company_id
    ON account(company_id);

ALTER TABLE company_product
    ADD CONSTRAINT fk_company_product_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE cash_registers
    ADD CONSTRAINT fk_cash_registers_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE warehouse
    ADD CONSTRAINT fk_warehouse_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE sale
    ADD CONSTRAINT fk_sale_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE goals
    ADD CONSTRAINT fk_goals_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE fidelity_policy
    ADD CONSTRAINT fk_fidelity_policy_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE stock_entries
    ADD CONSTRAINT fk_stock_entries_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE pos_device_session
    ADD CONSTRAINT fk_pos_device_session_company
        FOREIGN KEY (company_id) REFERENCES company(id);

ALTER TABLE mercadopago_caixa
    ADD CONSTRAINT fk_mercadopago_caixa_cash_register
        FOREIGN KEY (cash_register_id) REFERENCES cash_registers(id);

ALTER TABLE mercadopago_company
    ADD CONSTRAINT uq_mercadopago_company_company_id
        UNIQUE (company_id);
