-- V19 — Create support schema and initial tables
-- Module: kalles-support (migrated into kalles-sale)

CREATE SCHEMA IF NOT EXISTS support;

-- ---------------------------------------------------------------
-- users: customers who open tickets
-- ---------------------------------------------------------------
CREATE TABLE support.users (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_email UNIQUE (email)
);

-- ---------------------------------------------------------------
-- agents: support staff who handle tickets
-- ---------------------------------------------------------------
CREATE TABLE support.agents (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    employee_id VARCHAR(100) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT now(),

    CONSTRAINT uq_agents_employee_id UNIQUE (employee_id)
);

-- ---------------------------------------------------------------
-- categories: classify tickets and define their default priority
-- ---------------------------------------------------------------
CREATE TABLE support.categories (
    id               UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    subcategory      VARCHAR(150) NOT NULL,
    default_priority VARCHAR(20) NOT NULL,
    active           BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT now(),

    CONSTRAINT uq_categories_name_subcategory UNIQUE (name, subcategory),
    CONSTRAINT chk_categories_priority CHECK (default_priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- ---------------------------------------------------------------
-- tickets: core aggregate of the helpdesk domain
-- ---------------------------------------------------------------
CREATE TABLE support.tickets (
    id              UUID         NOT NULL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT         NOT NULL,
    status          VARCHAR(30)  NOT NULL,
    priority        VARCHAR(20)  NOT NULL,
    user_id         UUID         NOT NULL,
    agent_id        UUID,
    category_id     UUID         NOT NULL,
    sla_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    sla_started_at  TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_tickets_user     FOREIGN KEY (user_id)     REFERENCES support.users(id),
    CONSTRAINT fk_tickets_agent    FOREIGN KEY (agent_id)    REFERENCES support.agents(id),
    CONSTRAINT fk_tickets_category FOREIGN KEY (category_id) REFERENCES support.categories(id),

    CONSTRAINT chk_tickets_status   CHECK (status   IN ('OPEN', 'IN_PROGRESS', 'WAITING_FOR_CUSTOMER', 'RESOLVED', 'CLOSED')),
    CONSTRAINT chk_tickets_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX idx_tickets_status      ON support.tickets (status);
CREATE INDEX idx_tickets_user_id     ON support.tickets (user_id);
CREATE INDEX idx_tickets_agent_id    ON support.tickets (agent_id);
CREATE INDEX idx_tickets_category_id ON support.tickets (category_id);

-- ---------------------------------------------------------------
-- interactions: timeline of messages and notes
-- ---------------------------------------------------------------
CREATE TABLE support.interactions (
    id         UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    ticket_id  UUID      NOT NULL,
    content    TEXT      NOT NULL,
    type       VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_interactions_ticket FOREIGN KEY (ticket_id) REFERENCES support.tickets(id) ON DELETE CASCADE,
    CONSTRAINT chk_interactions_type CHECK (type IN ('CUSTOMER_MESSAGE', 'AGENT_MESSAGE', 'INTERNAL_NOTE'))
);

CREATE INDEX idx_interactions_ticket_id ON support.interactions (ticket_id);

-- ---------------------------------------------------------------
-- Seed: common categories
-- ---------------------------------------------------------------
INSERT INTO support.categories (name, subcategory, default_priority) VALUES
    ('System',   'Bug',             'HIGH'),
    ('System',   'Performance',     'MEDIUM'),
    ('System',   'Feature Request', 'LOW'),
    ('Billing',  'Charge Dispute',  'HIGH'),
    ('Billing',  'Invoice',         'MEDIUM'),
    ('Account',  'Access',          'HIGH'),
    ('Account',  'Password Reset',  'LOW'),
    ('General',  'Question',        'LOW'),
    ('General',  'Feedback',        'LOW');
