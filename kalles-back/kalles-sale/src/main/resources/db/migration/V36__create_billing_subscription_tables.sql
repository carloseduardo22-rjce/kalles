CREATE TABLE billing_subscription (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenant(id),
    provider VARCHAR(30) NOT NULL,
    external_customer_id VARCHAR(255),
    external_subscription_id VARCHAR(255),
    external_checkout_session_id VARCHAR(255),
    external_price_id VARCHAR(255),
    external_product_id VARCHAR(255),
    plan_code VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    billing_interval VARCHAR(30) NOT NULL,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
    last_event_id VARCHAR(255)
);

CREATE UNIQUE INDEX uk_billing_subscription_external_subscription_id
    ON billing_subscription (external_subscription_id);

CREATE UNIQUE INDEX uk_billing_subscription_external_customer_id
    ON billing_subscription (external_customer_id);

CREATE TABLE billing_webhook_event (
    id UUID PRIMARY KEY,
    provider VARCHAR(30) NOT NULL,
    external_event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL
);
