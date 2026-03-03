CREATE TABLE sale_audit_events (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id          UUID        NOT NULL REFERENCES sale (id),
    event_type       VARCHAR(50) NOT NULL,
    product_id       UUID        REFERENCES product (id),
    quantity_removed INTEGER,
    requested_by_id  UUID        NOT NULL REFERENCES operators (id),
    authorized_by_id UUID        REFERENCES operators (id),
    occurred_at      TIMESTAMP   NOT NULL
);

CREATE INDEX idx_audit_sale_id ON sale_audit_events (sale_id);
