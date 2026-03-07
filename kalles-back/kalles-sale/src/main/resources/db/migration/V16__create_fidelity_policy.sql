CREATE TABLE fidelity_policy (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    objective_points    INTEGER         NOT NULL,
    configured_discount NUMERIC(10, 2)  NOT NULL,
    value_point         INTEGER         NOT NULL,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATE            NOT NULL DEFAULT CURRENT_DATE
);
