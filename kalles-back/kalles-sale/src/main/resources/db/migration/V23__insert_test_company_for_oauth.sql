-- Inserindo uma empresa de teste para o fluxo do Mercado Pago OAuth
INSERT INTO mercadopago_company (id, external_id, name)
VALUES (gen_random_uuid(), 'ID_DA_LOJA_TESTE_123', 'Mercadinho do Teste')
ON CONFLICT (external_id) DO NOTHING;
