-- 1. Cria a tabela de Tenant que representa o dono/assinante do sistema
CREATE TABLE tenant (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    mp_access_token VARCHAR(255),
    mp_refresh_token VARCHAR(255),
    mp_user_id VARCHAR(60)
);

-- 2. Adiciona o vínculo da Company (Loja) com o Tenant
ALTER TABLE mercadopago_company ADD COLUMN tenant_id UUID;
ALTER TABLE mercadopago_company ADD CONSTRAINT fk_company_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id);

-- 3. Remove as credencias do MP da Company (Loja) para que fiquem concentradas no Tenant
ALTER TABLE mercadopago_company DROP COLUMN mp_access_token;
ALTER TABLE mercadopago_company DROP COLUMN mp_refresh_token;
ALTER TABLE mercadopago_company DROP COLUMN mp_user_id;

-- 4. Cria um Tenant de teste
INSERT INTO tenant (id, name) VALUES ('123e4567-e89b-12d3-a456-426614174000', 'Conta de Teste Kalles');

-- 5. Atualiza a Company de teste para pertencer a este Tenant
UPDATE mercadopago_company SET tenant_id = '123e4567-e89b-12d3-a456-426614174000' WHERE external_id = 'ID_DA_LOJA_TESTE_123';