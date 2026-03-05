CREATE TABLE warehouse (
    id      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name    VARCHAR(150)    NOT NULL,
    address VARCHAR(255),
    active  BOOLEAN         NOT NULL DEFAULT TRUE
);
