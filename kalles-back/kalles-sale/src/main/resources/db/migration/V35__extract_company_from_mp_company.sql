-- Cria a tabela base da empresa no Core (desacoplada do Mercado Pago)
CREATE TABLE company (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    street_name VARCHAR(255),
    street_number VARCHAR(255),
    city_name VARCHAR(255),
    state_name VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

-- Mapeia todo o schema que estava no mercadopago pra o core
INSERT INTO company(id, name, tenant_id, street_name, street_number, city_name, state_name, latitude, longitude)
SELECT id, name, tenant_id, street_name, street_number, city_name, state_name, latitude, longitude
FROM mercadopago_company;

-- Adiciona a chave estrangeira do adapter MercadoPago apontando para o Core Company
ALTER TABLE mercadopago_company ADD COLUMN company_id UUID;

UPDATE mercadopago_company SET company_id = id;

ALTER TABLE mercadopago_company ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE mercadopago_company ADD CONSTRAINT fk_mp_company_company FOREIGN KEY(company_id) REFERENCES company(id);

-- Limpa campos não mais usados na tabela mercadopago
ALTER TABLE mercadopago_company 
  DROP COLUMN name,
  DROP COLUMN street_name,
  DROP COLUMN street_number,
  DROP COLUMN city_name,
  DROP COLUMN state_name,
  DROP COLUMN latitude,
  DROP COLUMN longitude,
  DROP COLUMN tenant_id;

-- Desacopla o caixa do MP passando a usar o ID interno do Caixa core
ALTER TABLE mercadopago_caixa ADD COLUMN cash_register_id UUID;
ALTER TABLE mercadopago_caixa DROP COLUMN name;
ALTER TABLE mercadopago_caixa DROP COLUMN company_id;