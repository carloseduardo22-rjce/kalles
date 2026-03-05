CREATE TABLE location (
    id           UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID            NOT NULL REFERENCES warehouse (id),
    code         VARCHAR(100)    NOT NULL,
    description  VARCHAR(255)
);

CREATE INDEX idx_location_warehouse_id ON location (warehouse_id);
