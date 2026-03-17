CREATE TABLE mercadopago_company (
    id UUID,
    name VARCHAR(255),
    street_name VARCHAR(255),
    street_number VARCHAR(255),
    city_name VARCHAR(255),
    state_name VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    mp_store_id BIGINT
);
