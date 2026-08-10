CREATE TABLE sensitive_contents (
    id             UUID         NOT NULL,
    token          VARCHAR(120) NOT NULL,
    encrypted_text TEXT         NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    account_id     UUID         NOT NULL,
    CONSTRAINT pk_sensitive_contents PRIMARY KEY (id),
    CONSTRAINT uk_sensitive_contents_token UNIQUE (token),
    CONSTRAINT fk_sensitive_contents_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE INDEX idx_sensitive_contents_account ON sensitive_contents (account_id);

CREATE TABLE mp_point_orders (
    order_id           VARCHAR(255) NOT NULL,
    payment_id         VARCHAR(255),
    status             VARCHAR(255),
    external_reference VARCHAR(255),
    amount             NUMERIC(19, 2),
    idempotency_key    VARCHAR(255),
    CONSTRAINT pk_mp_point_orders PRIMARY KEY (order_id)
);

CREATE INDEX idx_mp_point_orders_external_reference ON mp_point_orders (external_reference);

CREATE TABLE terminal (
    id              VARCHAR(255) NOT NULL,
    pos_id          VARCHAR(255),
    store_id        VARCHAR(255),
    external_pos_id VARCHAR(255),
    operating_mode  VARCHAR(255),
    CONSTRAINT pk_terminal PRIMARY KEY (id)
);
