ALTER TABLE mercadopago_company
ADD COLUMN mp_access_token VARCHAR(255),
ADD COLUMN mp_refresh_token VARCHAR(255),
ADD COLUMN mp_user_id VARCHAR(255);
