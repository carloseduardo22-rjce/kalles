CREATE TABLE operators (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    code            VARCHAR(255)    NOT NULL UNIQUE,
    permission_level VARCHAR(50)
);
