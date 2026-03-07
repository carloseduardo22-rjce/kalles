CREATE TABLE fidelity (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    points              INTEGER         NOT NULL DEFAULT 0,
    available_discount  NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    created_at          DATE            NOT NULL DEFAULT CURRENT_DATE,
    expired             BOOLEAN         NOT NULL DEFAULT FALSE,
    policy_id           UUID            NOT NULL REFERENCES fidelity_policy(id),
    client_id           UUID            NOT NULL UNIQUE REFERENCES client(id)
);

CREATE INDEX idx_fidelity_client_id ON fidelity (client_id);
