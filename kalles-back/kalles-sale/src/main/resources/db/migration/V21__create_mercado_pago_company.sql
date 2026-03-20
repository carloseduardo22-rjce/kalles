CREATE TABLE mercadopago_company (
    id UUID PRIMARY KEY,
    external_id VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(255),
    street_name VARCHAR(255),
    street_number VARCHAR(255),
    city_name VARCHAR(255),
    state_name VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    mp_store_id BIGINT
);

CREATE TABLE mercadopago_caixa (
    id UUID PRIMARY KEY,
    external_id VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(255),
    company_id VARCHAR(60),
    mp_pos_id BIGINT
);
