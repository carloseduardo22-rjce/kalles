-- =============================================================
-- Kalles PDV — Seed de dados para testes
-- Migração repetível Flyway (R__): executa após todas as migrações
-- versionadas (V1–V18) e re-executa quando o conteúdo do arquivo muda.
-- Todos os INSERTs usam ON CONFLICT ... DO NOTHING, portanto é idempotente.
-- =============================================================

-- ---------------------------------------------------------------
-- 0. EMPRESA E TENANT DE TESTE
-- ---------------------------------------------------------------
INSERT INTO tenant (id, name) VALUES ('123e4567-e89b-12d3-a456-426614174000', 'Conta de Teste Kalles') ON CONFLICT (id) DO NOTHING;

-- Remove o seed legado criado nas migrations V23/V35 para manter apenas
-- uma empresa canônica de testes locais por tenant.
DELETE FROM mercadopago_company
WHERE external_id = 'ID_DA_LOJA_TESTE_123'
  AND id <> 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f';

DELETE FROM company
WHERE tenant_id = '123e4567-e89b-12d3-a456-426614174000'
  AND name = 'Mercadinho do Teste'
  AND id <> 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f';

-- Inserindo a loja (como entidade segregada do Core)
INSERT INTO company (id, name, tenant_id) VALUES ('e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f', 'Loja Matriz', '123e4567-e89b-12d3-a456-426614174000') ON CONFLICT (id) DO NOTHING;

-- Inserindo o vínculo com o Mercado Pago
INSERT INTO mercadopago_company (id, company_id, external_id)
VALUES (
  'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f',
  'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f',
  'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'
)
ON CONFLICT (id) DO UPDATE
SET
  company_id = EXCLUDED.company_id,
  external_id = EXCLUDED.external_id;

-- ---------------------------------------------------------------
-- 1. OPERADORES
--    Linkando Operadores à Empresa Matriz
-- ---------------------------------------------------------------
INSERT INTO operators (name, code, permission_level) VALUES
  ('João Silva',           'OP-001',   'BASIC'),
  ('Maria Santos',         'OP-002',   'SUPERVISOR'),
  ('Pedro Costa',          'OP-003',   'MANAGER'),
  ('Administrador Kalles', 'ADMIN-001','ADMIN'),
  ('Lucas Oliveira',       'OP-004',   'BASIC'),
  ('Isabela Rocha',        'OP-005',   'BASIC'),
  ('Rafael Alves',         'OP-006',   'SUPERVISOR')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. CAIXAS
-- ---------------------------------------------------------------
INSERT INTO cash_registers (code, description, active, company_id) VALUES
  ('CAIXA-01', 'Caixa principal — frente de loja',  true, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'),
  ('CAIXA-02', 'Caixa secundário — balcão',         true, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'),
  ('CAIXA-03', 'Caixa self-checkout — Setor A',     true, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------
-- 3. PRODUTOS (Catálogo Global)
-- ---------------------------------------------------------------
INSERT INTO product (name, internal_code, barcode, description, tenant_id, version) VALUES
  ('Coca-Cola 350ml',        'PRD-001', '7891000100103', 'Refrigerante lata 350ml',                            '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Água Mineral 500ml',     'PRD-002', '7891000200204', 'Água sem gás 500ml',                                 '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Salgadinho 50g',         'PRD-003', '7891000300305', 'Salgadinho sabor queijo 50g',                        '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Chocolate 100g',         'PRD-004', '7891000400406', 'Barra de chocolate ao leite 100g',                   '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Biscoito Cream',         'PRD-005', '7891000500507', 'Biscoito cream cracker 200g',                        '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Suco de Laranja',        'PRD-006', '7891000600608', 'Néctar de laranja 1L',                               '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Arroz 5kg',              'PRD-007', '7891000700709', 'Arroz branco tipo 1, 5kg',                          '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Feijão 1kg',             'PRD-008', '7891000800800', 'Feijão carioca 1kg',                                '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Óleo de Soja 900ml',     'PRD-009', '7891000900901', 'Óleo de soja refinado 900ml',                        '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Macarrão 500g',          'PRD-010', '7891001001002', 'Macarrão espaguete 500g',                            '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Leite Integral 1L',      'PRD-011', '7891000110011', 'Leite integral tipo A 1L',                           '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Manteiga 200g',          'PRD-012', '7891000120012', 'Manteiga extra creme sem sal 200g',                  '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Iogurte Natural 170g',   'PRD-013', '7891000130013', 'Iogurte natural integral 170g',                      '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Pão de Forma 500g',      'PRD-014', '7891000140014', 'Pão de forma tradicional 500g',                      '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Café Solúvel 50g',       'PRD-015', '7891000150015', 'Café solúvel instantâneo 50g',                       '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Açúcar Refinado 1kg',    'PRD-016', '7891000160016', 'Açúcar refinado branco 1kg',                         '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Sabonete 90g',           'PRD-017', '7891000170017', 'Sabonete hidratante original 90g',                   '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Shampoo 200ml',          'PRD-018', '7891000180018', 'Shampoo restauração 200ml',                         '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Detergente 500ml',       'PRD-019', '7891000190019', 'Detergente neutro 500ml',                            '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Papel Higiênico 4un',    'PRD-020', '7891000200020', 'Papel higiênico dupla folha 4 rolos',               '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Amaciante 2L',           'PRD-021', '7891000210021', 'Amaciante concentrado 2L',                          '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Desinfetante 1L',        'PRD-022', '7891000220022', 'Desinfetante pinho 1L',                              '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Refrigerante Cola 2L',   'PRD-023', '7891000230023', 'Refrigerante cola 2L',                               '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Suco de Uva 1L',         'PRD-024', '7891000240024', 'Suco de uva integral 1L',                           '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Cereal Matinal 300g',    'PRD-025', '7891000250025', 'Cereal matinal de milho 300g',                       '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Fermento em Pó 100g',    'PRD-026', '7891000260026', 'Fermento químico em pó 100g',                        '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Vinagre 750ml',          'PRD-027', '7891000270027', 'Vinagre de álcool 750ml',                            '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Creme de Leite 300g',    'PRD-028', '7891000280028', 'Creme de leite mesa 300g',                           '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Biscoito Recheado 130g', 'PRD-029', '7891000290029', 'Biscoito recheado chocolate — linha descontinuada',  '123e4567-e89b-12d3-a456-426614174000', 0),
  ('Achocolatado 200ml',     'PRD-030', '7891000300030', 'Achocolatado UHT 200ml',                             '123e4567-e89b-12d3-a456-426614174000', 0)
ON CONFLICT (internal_code) DO NOTHING;

-- ---------------------------------------------------------------
-- 3.1. PRODUTOS DA EMPRESA (Preços Locais)
-- ---------------------------------------------------------------
INSERT INTO company_product (company_id, product_id, price, active, cost_price)
SELECT 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f', p.id, v.price, v.active, v.cost_price
FROM product p
JOIN (VALUES
  ('PRD-001', 5.50,  true, 3.85),
  ('PRD-002', 2.00,  true, 1.40),
  ('PRD-003', 3.75,  true, 2.63),
  ('PRD-004', 6.90,  true, 4.83),
  ('PRD-005', 4.50,  true, 3.15),
  ('PRD-006', 7.00,  true, 4.90),
  ('PRD-007', 22.90, true, 16.03),
  ('PRD-008', 10.50, true, 7.35),
  ('PRD-009', 7.80,  true, 5.46),
  ('PRD-010', 4.20,  true, 2.94),
  ('PRD-011', 4.50,  true, 3.15),
  ('PRD-012', 8.90,  true, 6.23),
  ('PRD-013', 3.20,  true, 2.24),
  ('PRD-014', 7.50,  true, 5.25),
  ('PRD-015', 6.80,  true, 4.76),
  ('PRD-016', 4.00,  true, 2.80),
  ('PRD-017', 3.50,  true, 2.45),
  ('PRD-018', 12.90, true, 9.03),
  ('PRD-019', 2.50,  true, 1.75),
  ('PRD-020', 10.90, true, 7.63),
  ('PRD-021', 15.90, true, 11.13),
  ('PRD-022', 5.40,  true, 3.78),
  ('PRD-023', 8.50,  true, 5.95),
  ('PRD-024', 11.00, true, 7.70),
  ('PRD-025', 9.90,  true, 6.93),
  ('PRD-026', 2.80,  true, 1.96),
  ('PRD-027', 3.10,  true, 2.17),
  ('PRD-028', 5.20,  true, 3.64),
  ('PRD-029', 3.90,  false, 2.73),
  ('PRD-030', 2.90,  true, 2.03)
) AS v(code, price, active, cost_price) ON p.internal_code = v.code
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
-- 4. DEPÓSITOS
-- ---------------------------------------------------------------
INSERT INTO warehouse (name, address, active, company_id) VALUES
  ('Depósito Central',  'Rua das Flores, 100 — Bairro Industrial', true, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'),
  ('Depósito Auxiliar', 'Av. Brasil, 500 — Galpão 3',             true, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
-- 5. LOCALIZAÇÕES  (por depósito)
-- ---------------------------------------------------------------
INSERT INTO location (warehouse_id, code, description)
SELECT w.id, loc.code, loc.description
FROM warehouse w
JOIN (VALUES
  ('Depósito Central',  'A1', 'Corredor A — Prateleira 1'),
  ('Depósito Central',  'A2', 'Corredor A — Prateleira 2'),
  ('Depósito Central',  'B1', 'Corredor B — Prateleira 1'),
  ('Depósito Auxiliar', 'X1', 'Setor X — Prateleira 1'),
  ('Depósito Auxiliar', 'X2', 'Setor X — Prateleira 2')
) AS loc(warehouse_name, code, description)
  ON w.name = loc.warehouse_name
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
-- 6. ESTOQUE
-- ---------------------------------------------------------------
INSERT INTO stock (product_id, location_id, quantity, version)
SELECT p.id, l.id, qty.quantity, 0
FROM (
  VALUES
    ('PRD-001', 'Depósito Central',  'A1',  80),
    ('PRD-001', 'Depósito Auxiliar', 'X1',  20),
    ('PRD-002', 'Depósito Central',  'A1',  60),
    ('PRD-002', 'Depósito Auxiliar', 'X1',  40),
    ('PRD-003', 'Depósito Central',  'A2', 100),
    ('PRD-004', 'Depósito Central',  'A2',  50),
    ('PRD-004', 'Depósito Auxiliar', 'X2',  50),
    ('PRD-005', 'Depósito Central',  'B1', 100),
    ('PRD-006', 'Depósito Central',  'B1',  70),
    ('PRD-006', 'Depósito Auxiliar', 'X2',  30),
    ('PRD-007', 'Depósito Central',  'A1',  90),
    ('PRD-008', 'Depósito Central',  'A2',  85),
    ('PRD-009', 'Depósito Central',  'B1',  60),
    ('PRD-010', 'Depósito Auxiliar', 'X1', 100),
    ('PRD-011', 'Depósito Central',  'A1', 120),
    ('PRD-012', 'Depósito Central',  'A2',  60),
    ('PRD-013', 'Depósito Central',  'A2',  80),
    ('PRD-014', 'Depósito Central',  'B1',  50),
    ('PRD-015', 'Depósito Auxiliar', 'X1',  90),
    ('PRD-016', 'Depósito Central',  'A1', 100),
    ('PRD-017', 'Depósito Auxiliar', 'X2', 150),
    ('PRD-018', 'Depósito Auxiliar', 'X2',  80),
    ('PRD-019', 'Depósito Central',  'B1', 200),
    ('PRD-020', 'Depósito Central',  'B1', 100),
    ('PRD-021', 'Depósito Auxiliar', 'X1',  60),
    ('PRD-022', 'Depósito Central',  'A2',  70),
    ('PRD-023', 'Depósito Central',  'A1',  90),
    ('PRD-024', 'Depósito Auxiliar', 'X2',  45),
    ('PRD-025', 'Depósito Central',  'B1',  55),
    ('PRD-030', 'Depósito Central',  'A2',  60)
) AS qty(internal_code, warehouse_name, location_code, quantity)
JOIN product   p ON p.internal_code = qty.internal_code
JOIN warehouse w ON w.name          = qty.warehouse_name
JOIN location  l ON l.warehouse_id  = w.id AND l.code = qty.location_code
ON CONFLICT (product_id, location_id) DO NOTHING;

-- ---------------------------------------------------------------
-- 7. CLIENTES
-- ---------------------------------------------------------------
INSERT INTO client (name, birth_date, gender, cpf, code_country, cellphone, rg, name_father, name_mother, observations) VALUES
  ('Ana Paula Souza',    '1992-03-15', 'F', '529.982.247-25', '+55', '11991110001', '12345678', 'Carlos Souza',    'Marta Souza',    NULL),
  ('Bruno Ferreira',     '1985-07-22', 'M', '153.509.460-56', '+55', '21992220002', '23456789', 'Luiz Ferreira',   'Rosa Ferreira',  'Cliente VIP'),
  ('Carla Mendes',       '1998-11-05', 'F', '046.270.070-54', '+55', '31993330003', '34567890', NULL,              'Sônia Mendes',   NULL),
  ('Diego Lima',         '1990-01-30', 'M', '168.995.350-09', '+55', '41994440004', '45678901', 'Paulo Lima',      'Clara Lima',     NULL),
  ('Fernanda Castro',    '2000-08-18', 'F', '942.836.530-06', '+55', '51995550005', NULL,        NULL,             NULL,             NULL),
  ('Gabriela Martins',   '1995-04-10', 'F', '871.504.977-73', '+55', '61996660006', '56789012', 'Roberto Martins', 'Lúcia Martins',  'Preferência: produtos naturais'),
  ('Henrique Barbosa',   '1980-12-20', 'M', '023.845.231-40', '+55', '71997770007', '67890123', 'Sérgio Barbosa',  'Vera Barbosa',   'Cliente desde 2020'),
  ('Juliana Peixoto',    '1993-06-03', 'F', '470.645.218-07', '+55', '81998880008', '78901234', NULL,              'Beatriz Peixoto',NULL),
  ('Marcos Teixeira',    '1975-09-14', 'M', '862.073.580-01', '+55', '19999990009', '89012345', 'André Teixeira',  'Fátima Teixeira','Empresário — compras mensais'),
  ('Natália Rodrigues',  '2002-02-28', 'F', '674.594.981-49', '+55', '27900010010', NULL,        NULL,             NULL,             NULL)
ON CONFLICT (cpf) DO NOTHING;

-- ---------------------------------------------------------------
-- 8. SESSÃO DE CAIXA ABERTA
--    CAIXA-01 com João Silva, R$100 iniciais, status OPEN.
--    Permite entrar direto no PDV sem precisar abrir o caixa via UI.
-- ---------------------------------------------------------------
INSERT INTO cash_register_sessions (id, cash_register_id, operator_id, initial_amount, opened_at, closed_at, status)
SELECT
  'b0000000-0000-0000-0000-000000000001'::uuid,
  cr.id,
  op.id,
  100.00,
  NOW(),
  NULL,
  'OPEN'
FROM cash_registers cr, operators op
WHERE cr.code = 'CAIXA-01'
  AND op.code = 'OP-001'
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 9. POLÍTICAS DE FIDELIDADE
--    IDs fixos para que os re-runs repetem sem duplicar.
--    Política antiga (inativa): 50 pts → R$10,00 de desconto.
--    Política atual  (ativa):  100 pts → R$20,00 de desconto.
-- ---------------------------------------------------------------
INSERT INTO fidelity_policy (id, objective_points, configured_discount, value_point, active, created_at, company_id)
VALUES
  ('a1b2c3d4-0001-0001-0001-000000000001'::uuid,  50, 10.00, 1, false, '2025-09-01', 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'),
  ('a1b2c3d4-0002-0002-0002-000000000002'::uuid, 100, 20.00, 1, true,  '2026-01-01', 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 10. INSCRIÇÕES NO PROGRAMA DE FIDELIDADE
--    Clientes em diferentes estágios para testar todos os cenários:
--      Ana Paula  → 70 pts acumulados, sem desconto disponível (quase na meta)
--      Bruno      →  0 pts, R$20 de desconto disponível (meta atingida, aguardando uso)
--      Carla      → 35 pts, sem desconto (em progresso)
--      Gabriela   →  0 pts, R$20 de desconto disponível (meta exata)
--      Henrique   → 10 pts, recém inscrito
-- ---------------------------------------------------------------
INSERT INTO fidelity (id, points, available_discount, created_at, expired, policy_id, client_id)
SELECT
  gen_random_uuid(),
  v.pts,
  v.disc::numeric(10,2),
  v.enrolled::date,
  false,
  'a1b2c3d4-0002-0002-0002-000000000002'::uuid,
  c.id
FROM (VALUES
  ('529.982.247-25',  70,  0.00, '2026-01-15'),
  ('153.509.460-56',   0, 20.00, '2026-01-20'),
  ('046.270.070-54',  35,  0.00, '2026-02-01'),
  ('871.504.977-73',   0, 20.00, '2026-02-10'),
  ('023.845.231-40',  10,  0.00, '2026-03-01')
) AS v(cpf, pts, disc, enrolled)
JOIN client c ON c.cpf = v.cpf
ON CONFLICT (client_id) DO NOTHING;

-- ---------------------------------------------------------------
-- 11. SESSÃO FECHADA (dia anterior)
--     CAIXA-02, Pedro Costa (MANAGER), 06/03/2026.
--     Permite visualizar sessão histórica na tela de caixas.
-- ---------------------------------------------------------------
INSERT INTO cash_register_sessions (id, cash_register_id, operator_id, initial_amount, opened_at, closed_at, status)
SELECT
  'b0000000-0000-0000-0000-000000000002'::uuid,
  cr.id,
  op.id,
  200.00,
  '2026-03-06 08:00:00',
  '2026-03-06 17:30:00',
  'CLOSED'
FROM cash_registers cr, operators op
WHERE cr.code = 'CAIXA-02'
  AND op.code = 'OP-003'
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 12. VENDAS — SESSÃO FECHADA (b0000000-...002)
--
--   Venda A | Bruno Ferreira | COMPLETED | desconto fidelidade R$20,00
--           | subtotal=24,00 | total=4,00 | pago em dinheiro
--
--   Venda B | Ana Paula Souza | COMPLETED | fidelidade sem desconto (70 pts)
--           | subtotal=16,50 | total=16,50 | pago no débito
--
--   Venda C | sem cliente | COMPLETED | pagamento via PIX
--           | subtotal=33,40 | total=33,40
--
--   Venda D | sem cliente | CANCELED
-- ---------------------------------------------------------------
INSERT INTO sale (id, version, session_token, state, client_id,
                  subtotal, total, amount_due,
                  fidelity_discount_applied, points_earned, company_id)
SELECT
  v.sale_id::uuid,
  0,
  'b0000000-0000-0000-0000-000000000002',
  v.state,
  c.id,
  v.subtotal::numeric(19,2),
  v.total::numeric(19,2),
  v.amount_due::numeric(19,2),
  v.fid_disc::numeric(19,2),
  0,
  'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid
FROM (VALUES
  ('c1000000-0000-0000-0000-000000000001', 'COMPLETED', '153.509.460-56', 24.00, 4.00,  0.00, 20.00),
  ('c1000000-0000-0000-0000-000000000002', 'COMPLETED', '529.982.247-25', 16.50, 16.50, 0.00,  0.00),
  ('c1000000-0000-0000-0000-000000000003', 'COMPLETED', NULL,             33.40, 33.40, 0.00,  0.00),
  ('c1000000-0000-0000-0000-000000000004', 'CANCELED',  NULL,              6.90,  6.90, 6.90,  0.00)
) AS v(sale_id, state, cpf, subtotal, total, amount_due, fid_disc)
LEFT JOIN client c ON c.cpf = v.cpf
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 13. VENDAS — SESSÃO ABERTA (b0000000-...001)
--     Permite que a tela de Relatórios mostre dados do turno atual.
--
--   Venda E | Gabriela Martins | COMPLETED | desconto fidelidade R$20,00
--           | subtotal=20,20 | total=0,20 | pago em dinheiro
--
--   Venda F | sem cliente | COMPLETED | cartão de crédito
--           | subtotal=36,40 | total=36,40
--
--   Venda G | sem cliente | CANCELED
-- ---------------------------------------------------------------
INSERT INTO sale (id, version, session_token, state, client_id,
                  subtotal, total, amount_due,
                  fidelity_discount_applied, points_earned, company_id)
SELECT
  v.sale_id::uuid,
  0,
  'b0000000-0000-0000-0000-000000000001',
  v.state,
  c.id,
  v.subtotal::numeric(19,2),
  v.total::numeric(19,2),
  v.amount_due::numeric(19,2),
  v.fid_disc::numeric(19,2),
  0,
  'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid
FROM (VALUES
  ('c1000000-0000-0000-0000-000000000005', 'COMPLETED', '871.504.977-73', 20.20, 0.20,  0.00, 20.00),
  ('c1000000-0000-0000-0000-000000000006', 'COMPLETED', NULL,             36.40, 36.40, 0.00,  0.00),
  ('c1000000-0000-0000-0000-000000000007', 'CANCELED',  NULL,              4.50,  4.50, 4.50,  0.00)
) AS v(sale_id, state, cpf, subtotal, total, amount_due, fid_disc)
LEFT JOIN client c ON c.cpf = v.cpf
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 14. ITENS DAS VENDAS
--     Colunas: sale_id, product_id, quantity, unit_price, discount
-- ---------------------------------------------------------------
INSERT INTO sale_item (id, sale_id, product_id, quantity, unit_price, discount)
SELECT
  i.item_id::uuid,
  i.sale_id::uuid,
  p.id,
  i.qty,
  i.unit_price::numeric(19,2),
  i.discount::numeric(19,2)
FROM (VALUES
  -- Venda A (Bruno + fidelidade)
  ('d1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'PRD-001', 3, 5.50, 0.00),
  ('d1000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'PRD-003', 2, 3.75, 0.00),
  -- Venda B (Ana Paula)
  ('d1000000-0000-0000-0000-000000000003', 'c1000000-0000-0000-0000-000000000002', 'PRD-011', 2, 4.50, 0.00),
  ('d1000000-0000-0000-0000-000000000004', 'c1000000-0000-0000-0000-000000000002', 'PRD-014', 1, 7.50, 0.00),
  -- Venda C (sem cliente, PIX)
  ('d1000000-0000-0000-0000-000000000005', 'c1000000-0000-0000-0000-000000000003', 'PRD-007', 1, 22.90, 0.00),
  ('d1000000-0000-0000-0000-000000000006', 'c1000000-0000-0000-0000-000000000003', 'PRD-008', 1, 10.50, 0.00),
  -- Venda D (cancelada)
  ('d1000000-0000-0000-0000-000000000007', 'c1000000-0000-0000-0000-000000000004', 'PRD-004', 1, 6.90, 0.00),
  -- Venda E (Gabriela + fidelidade)
  ('d1000000-0000-0000-0000-000000000008', 'c1000000-0000-0000-0000-000000000005', 'PRD-004', 2, 6.90, 0.00),
  ('d1000000-0000-0000-0000-000000000009', 'c1000000-0000-0000-0000-000000000005', 'PRD-013', 2, 3.20, 0.00),
  -- Venda F (sem cliente, crédito)
  ('d1000000-0000-0000-0000-000000000010', 'c1000000-0000-0000-0000-000000000006', 'PRD-007', 1, 22.90, 0.00),
  ('d1000000-0000-0000-0000-000000000011', 'c1000000-0000-0000-0000-000000000006', 'PRD-011', 3, 4.50, 0.00),
  -- Venda G (cancelada)
  ('d1000000-0000-0000-0000-000000000012', 'c1000000-0000-0000-0000-000000000007', 'PRD-005', 1, 4.50, 0.00)
) AS i(item_id, sale_id, internal_code, qty, unit_price, discount)
JOIN product p ON p.internal_code = i.internal_code
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 15. PAGAMENTOS DAS VENDAS CONCLUÍDAS
-- ---------------------------------------------------------------
INSERT INTO payment (id, sale_id, method, amount, change_amount, transaction_id, confirmed, created_at, updated_at)
VALUES
  -- Venda A — CASH R$5,00 troco R$1,00 (total R$4,00)
  ('e1000000-0000-0000-0000-000000000001',
   'c1000000-0000-0000-0000-000000000001'::uuid,
   'CASH', 5.00, 1.00, NULL, true, '2026-03-06 09:15:00', '2026-03-06 09:15:00'),
  -- Venda B — DEBIT_CARD R$16,50
  ('e1000000-0000-0000-0000-000000000002',
   'c1000000-0000-0000-0000-000000000002'::uuid,
   'DEBIT_CARD', 16.50, 0.00, 'TXN-DB-001', true, '2026-03-06 10:30:00', '2026-03-06 10:30:00'),
  -- Venda C — PIX R$33,40
  ('e1000000-0000-0000-0000-000000000003',
   'c1000000-0000-0000-0000-000000000003'::uuid,
   'PIX', 33.40, 0.00, 'TXN-PIX-001', true, '2026-03-06 11:45:00', '2026-03-06 11:45:00'),
  -- Venda E — CASH R$1,00 troco R$0,80 (total R$0,20)
  ('e1000000-0000-0000-0000-000000000005',
   'c1000000-0000-0000-0000-000000000005'::uuid,
   'CASH', 1.00, 0.80, NULL, true, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
  -- Venda F — CREDIT_CARD R$36,40
  ('e1000000-0000-0000-0000-000000000006',
   'c1000000-0000-0000-0000-000000000006'::uuid,
   'CREDIT_CARD', 36.40, 0.00, 'TXN-CC-001', true, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 16. SEGUNDA SESSÃO ABERTA — CAIXA-03
--     Lucas Oliveira (OP-004), R$80 de fundo inicial.
-- ---------------------------------------------------------------
INSERT INTO cash_register_sessions (id, cash_register_id, operator_id, initial_amount, opened_at, closed_at, status)
SELECT
  'b0000000-0000-0000-0000-000000000003'::uuid,
  cr.id,
  op.id,
  80.00,
  NOW() - INTERVAL '3 hours',
  NULL,
  'OPEN'
FROM cash_registers cr, operators op
WHERE cr.code = 'CAIXA-03'
  AND op.code = 'OP-004'
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 17. VENDAS EXTRAS — SESSÃO CAIXA-01 (b0000000-...001)
--     Enriquece o Resumo do Turno com mais métodos de pagamento.
--
--   Venda H | Diego Lima  | COMPLETED | subtotal=22.90, PIX
--   Venda I | sem cliente | COMPLETED | subtotal=14.00, CASH
--   Venda J | Natália     | COMPLETED | subtotal=12.90, CREDIT_CARD
--   Venda K | Carla Mendes| COMPLETED | subtotal=8.90,  DEBIT_CARD
-- ---------------------------------------------------------------
INSERT INTO sale (id, version, session_token, state, client_id,
                  subtotal, total, amount_due,
                  fidelity_discount_applied, points_earned, company_id)
SELECT
  v.sale_id::uuid,
  0,
  'b0000000-0000-0000-0000-000000000001',
  'COMPLETED',
  c.id,
  v.subtotal::numeric(19,2),
  v.total::numeric(19,2),
  0.00,
  0.00,
  0,
  'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid
FROM (VALUES
  ('c1000000-0000-0000-0000-000000000008', '168.995.350-09', 22.90, 22.90),
  ('c1000000-0000-0000-0000-000000000009', NULL,             14.00, 14.00),
  ('c1000000-0000-0000-0000-000000000010', '674.594.981-49', 12.90, 12.90),
  ('c1000000-0000-0000-0000-000000000011', '046.270.070-54',  8.90,  8.90)
) AS v(sale_id, cpf, subtotal, total)
LEFT JOIN client c ON c.cpf = v.cpf
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 18. VENDAS — SESSÃO CAIXA-03 (b0000000-...003)
--
--   Venda L | sem cliente    | COMPLETED | subtotal=33.40, PIX
--   Venda M | Marcos Teixeira| COMPLETED | subtotal=15.00, DEBIT_CARD
--   Venda N | sem cliente    | COMPLETED | subtotal=13.40, CASH
--   Venda O | sem cliente    | CANCELED
-- ---------------------------------------------------------------
INSERT INTO sale (id, version, session_token, state, client_id,
                  subtotal, total, amount_due,
                  fidelity_discount_applied, points_earned, company_id)
SELECT
  v.sale_id::uuid,
  0,
  'b0000000-0000-0000-0000-000000000003',
  v.state,
  c.id,
  v.subtotal::numeric(19,2),
  v.total::numeric(19,2),
  v.amount_due::numeric(19,2),
  0.00,
  0,
  'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid
FROM (VALUES
  ('c1000000-0000-0000-0000-000000000012', 'COMPLETED', NULL,             33.40, 33.40, 0.00),
  ('c1000000-0000-0000-0000-000000000013', 'COMPLETED', '862.073.580-01', 15.00, 15.00, 0.00),
  ('c1000000-0000-0000-0000-000000000014', 'COMPLETED', NULL,             13.40, 13.40, 0.00),
  ('c1000000-0000-0000-0000-000000000015', 'CANCELED',  NULL,              7.00,  7.00, 7.00)
) AS v(sale_id, state, cpf, subtotal, total, amount_due)
LEFT JOIN client c ON c.cpf = v.cpf
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 19. ITENS DAS VENDAS EXTRAS (seções 17 e 18)
-- ---------------------------------------------------------------
INSERT INTO sale_item (id, sale_id, product_id, quantity, unit_price, discount)
SELECT
  i.item_id::uuid,
  i.sale_id::uuid,
  p.id,
  i.qty,
  i.unit_price::numeric(19,2),
  0.00
FROM (VALUES
  -- Venda H: 1× Arroz 5kg
  ('d1000000-0000-0000-0000-000000000013', 'c1000000-0000-0000-0000-000000000008', 'PRD-007', 1, 22.90),
  -- Venda I: 2× Suco de Laranja
  ('d1000000-0000-0000-0000-000000000014', 'c1000000-0000-0000-0000-000000000009', 'PRD-006', 2, 7.00),
  -- Venda J: 1× Shampoo
  ('d1000000-0000-0000-0000-000000000015', 'c1000000-0000-0000-0000-000000000010', 'PRD-018', 1, 12.90),
  -- Venda K: 1× Manteiga
  ('d1000000-0000-0000-0000-000000000016', 'c1000000-0000-0000-0000-000000000011', 'PRD-012', 1, 8.90),
  -- Venda L: 1× Arroz + 1× Feijão
  ('d1000000-0000-0000-0000-000000000017', 'c1000000-0000-0000-0000-000000000012', 'PRD-007', 1, 22.90),
  ('d1000000-0000-0000-0000-000000000018', 'c1000000-0000-0000-0000-000000000012', 'PRD-008', 1, 10.50),
  -- Venda M: 1× Pão de Forma + 1× Café Solúvel
  ('d1000000-0000-0000-0000-000000000019', 'c1000000-0000-0000-0000-000000000013', 'PRD-014', 1, 7.50),
  ('d1000000-0000-0000-0000-000000000020', 'c1000000-0000-0000-0000-000000000013', 'PRD-015', 1, 6.80),
  -- Venda N: 1× Leite + 1× Açúcar + 1× Detergente
  ('d1000000-0000-0000-0000-000000000021', 'c1000000-0000-0000-0000-000000000014', 'PRD-011', 1, 4.50),
  ('d1000000-0000-0000-0000-000000000022', 'c1000000-0000-0000-0000-000000000014', 'PRD-016', 1, 4.00),
  ('d1000000-0000-0000-0000-000000000023', 'c1000000-0000-0000-0000-000000000014', 'PRD-019', 1, 2.50),
  -- Venda O: 1× Suco de Uva (cancelada)
  ('d1000000-0000-0000-0000-000000000024', 'c1000000-0000-0000-0000-000000000015', 'PRD-024', 1, 7.00)
) AS i(item_id, sale_id, internal_code, qty, unit_price)
JOIN product p ON p.internal_code = i.internal_code
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 20. PAGAMENTOS DAS VENDAS EXTRAS
-- ---------------------------------------------------------------
INSERT INTO payment (id, sale_id, method, amount, change_amount, transaction_id, confirmed, created_at, updated_at)
VALUES
  -- Venda H — PIX R$22,90
  ('e1000000-0000-0000-0000-000000000008',
   'c1000000-0000-0000-0000-000000000008'::uuid,
   'PIX', 22.90, 0.00, 'TXN-PIX-002', true, NOW() - INTERVAL '90 minutes', NOW() - INTERVAL '90 minutes'),
  -- Venda I — CASH R$15,00, troco R$1,00
  ('e1000000-0000-0000-0000-000000000009',
   'c1000000-0000-0000-0000-000000000009'::uuid,
   'CASH', 15.00, 1.00, NULL, true, NOW() - INTERVAL '75 minutes', NOW() - INTERVAL '75 minutes'),
  -- Venda J — CREDIT_CARD R$12,90
  ('e1000000-0000-0000-0000-000000000010',
   'c1000000-0000-0000-0000-000000000010'::uuid,
   'CREDIT_CARD', 12.90, 0.00, 'TXN-CC-002', true, NOW() - INTERVAL '60 minutes', NOW() - INTERVAL '60 minutes'),
  -- Venda K — DEBIT_CARD R$8,90
  ('e1000000-0000-0000-0000-000000000011',
   'c1000000-0000-0000-0000-000000000011'::uuid,
   'DEBIT_CARD', 8.90, 0.00, 'TXN-DB-002', true, NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '45 minutes'),
  -- Venda L — PIX R$33,40
  ('e1000000-0000-0000-0000-000000000012',
   'c1000000-0000-0000-0000-000000000012'::uuid,
   'PIX', 33.40, 0.00, 'TXN-PIX-003', true, NOW() - INTERVAL '2 hours 30 minutes', NOW() - INTERVAL '2 hours 30 minutes'),
  -- Venda M — DEBIT_CARD R$15,00
  ('e1000000-0000-0000-0000-000000000013',
   'c1000000-0000-0000-0000-000000000013'::uuid,
   'DEBIT_CARD', 15.00, 0.00, 'TXN-DB-003', true, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
  -- Venda N — CASH R$15,00, troco R$1,60
  ('e1000000-0000-0000-0000-000000000014',
   'c1000000-0000-0000-0000-000000000014'::uuid,
   'CASH', 15.00, 1.60, NULL, true, NOW() - INTERVAL '90 minutes', NOW() - INTERVAL '90 minutes')
ON CONFLICT (id) DO NOTHING;

-- ===============================================================
-- BLOCO II — HISTÓRICO DE SESSÕES PARA TESTES DE RELATÓRIOS
-- ---------------------------------------------------------------
-- 8 sessões FECHADAS distribuídas entre 03/03 e 09/03/2026.
-- Permite testar:
--   • Filtro "Data específica"  → ver sessões de um único dia
--   • Filtro "Intervalo"        → range multi-dia (ex.: seg–dom)
--   • Dias com 2+ caixas        → 03/03 (CAIXA-01 + CAIXA-02)
--                                  09/03 (CAIXA-01 + CAIXA-02)
--   • Dia de maior movimento    → 07/03 (CAIXA-01, 5 vendas)
-- IDs das sessões: b...004 a b...011
-- IDs das vendas:  c1...016 a c1...050
-- IDs dos itens:   d2...001 a d2...082
-- IDs pag.s:       e2...001 a e2...030
-- ===============================================================

-- ---------------------------------------------------------------
-- 21. SESSÕES HISTÓRICAS (CLOSED) — b004 a b011
-- ---------------------------------------------------------------
INSERT INTO cash_register_sessions
  (id, cash_register_id, operator_id, initial_amount, opened_at, closed_at, status)
SELECT
  s.session_id::uuid,
  cr.id,
  op.id,
  s.initial_amount::numeric(19,2),
  s.opened_at::timestamp,
  s.closed_at::timestamp,
  'CLOSED'
FROM (VALUES
  -- 03/03 — dois caixas em operação
  ('b0000000-0000-0000-0000-000000000004','CAIXA-01','OP-002',150.00,'2026-03-03 08:30:00','2026-03-03 18:15:00'),
  ('b0000000-0000-0000-0000-000000000005','CAIXA-02','OP-005',100.00,'2026-03-03 09:00:00','2026-03-03 17:45:00'),
  -- 04/03
  ('b0000000-0000-0000-0000-000000000006','CAIXA-01','OP-001',100.00,'2026-03-04 08:00:00','2026-03-04 18:00:00'),
  -- 05/03
  ('b0000000-0000-0000-0000-000000000007','CAIXA-03','OP-006', 80.00,'2026-03-05 09:30:00','2026-03-05 19:00:00'),
  -- 07/03 — maior movimento
  ('b0000000-0000-0000-0000-000000000008','CAIXA-01','OP-003',200.00,'2026-03-07 08:00:00','2026-03-07 20:00:00'),
  -- 08/03
  ('b0000000-0000-0000-0000-000000000009','CAIXA-02','OP-001',100.00,'2026-03-08 09:00:00','2026-03-08 17:30:00'),
  -- 09/03 — dois caixas em operação
  ('b0000000-0000-0000-0000-000000000010','CAIXA-01','OP-002',150.00,'2026-03-09 08:00:00','2026-03-09 19:00:00'),
  ('b0000000-0000-0000-0000-000000000011','CAIXA-02','OP-006', 80.00,'2026-03-09 10:00:00','2026-03-09 18:30:00')
) AS s(session_id, caixa_code, op_code, initial_amount, opened_at, closed_at)
JOIN cash_registers cr ON cr.code = s.caixa_code
JOIN operators      op ON op.code = s.op_code
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 22. VENDAS DAS SESSÕES HISTÓRICAS
-- ---------------------------------------------------------------
-- session_token = string do UUID da sessão correspondente.
-- Todas anônimas (client_id = NULL) — cenário típico de varejo.
-- subtotal = total para vendas concluídas; amount_due = 0.
-- Para canceladas: amount_due = subtotal.
INSERT INTO sale (id, version, session_token, state, client_id,
                  subtotal, total, amount_due, fidelity_discount_applied, points_earned, company_id)
VALUES
  -- ── b004 · 03/03 · CAIXA-01 · Maria Santos ──────────────────
  -- P: 3×Coca + 2×Salgadinho = 24,00   CASH   25,00 (troco 1,00)
  ('c1000000-0000-0000-0000-000000000016'::uuid,0,'b0000000-0000-0000-0000-000000000004','COMPLETED',NULL, 24.00, 24.00, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Q: Leite + Pão + Manteiga = 20,90   PIX
  ('c1000000-0000-0000-0000-000000000017'::uuid,0,'b0000000-0000-0000-0000-000000000004','COMPLETED',NULL, 20.90, 20.90, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- R: Arroz + Feijão = 33,40   CREDIT_CARD
  ('c1000000-0000-0000-0000-000000000018'::uuid,0,'b0000000-0000-0000-0000-000000000004','COMPLETED',NULL, 33.40, 33.40, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- S: 2×Água + Biscoito = 8,50   DEBIT_CARD
  ('c1000000-0000-0000-0000-000000000019'::uuid,0,'b0000000-0000-0000-0000-000000000004','COMPLETED',NULL,  8.50,  8.50, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- T: Chocolate (cancelada)
  ('c1000000-0000-0000-0000-000000000020'::uuid,0,'b0000000-0000-0000-0000-000000000004','CANCELED', NULL,  6.90,  6.90, 6.90,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),

  -- ── b005 · 03/03 · CAIXA-02 · Isabela Rocha ─────────────────
  -- U: Shampoo + Detergente = 15,40   PIX
  ('c1000000-0000-0000-0000-000000000021'::uuid,0,'b0000000-0000-0000-0000-000000000005','COMPLETED',NULL, 15.40, 15.40, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- V: Papel Higiênico + Amaciante = 26,80   CREDIT_CARD
  ('c1000000-0000-0000-0000-000000000022'::uuid,0,'b0000000-0000-0000-0000-000000000005','COMPLETED',NULL, 26.80, 26.80, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- W: Cereal + Leite = 14,40   CASH   15,00 (troco 0,60)
  ('c1000000-0000-0000-0000-000000000023'::uuid,0,'b0000000-0000-0000-0000-000000000005','COMPLETED',NULL, 14.40, 14.40, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),

  -- ── b006 · 04/03 · CAIXA-01 · João Silva ────────────────────
  -- X1: 2×Suco Laranja + Chocolate = 20,90   PIX
  ('c1000000-0000-0000-0000-000000000024'::uuid,0,'b0000000-0000-0000-0000-000000000006','COMPLETED',NULL, 20.90, 20.90, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- X2: Arroz + Óleo = 30,70   CASH   35,00 (troco 4,30)
  ('c1000000-0000-0000-0000-000000000025'::uuid,0,'b0000000-0000-0000-0000-000000000006','COMPLETED',NULL, 30.70, 30.70, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- X3: Sabonete + Detergente + Desinfetante = 11,40   DEBIT_CARD
  ('c1000000-0000-0000-0000-000000000026'::uuid,0,'b0000000-0000-0000-0000-000000000006','COMPLETED',NULL, 11.40, 11.40, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- X4: 2×Iogurte + Manteiga = 15,30   CREDIT_CARD
  ('c1000000-0000-0000-0000-000000000027'::uuid,0,'b0000000-0000-0000-0000-000000000006','COMPLETED',NULL, 15.30, 15.30, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- X5: Amaciante (cancelada)
  ('c1000000-0000-0000-0000-000000000028'::uuid,0,'b0000000-0000-0000-0000-000000000006','CANCELED', NULL, 15.90, 15.90,15.90,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),

  -- ── b007 · 05/03 · CAIXA-03 · Rafael Alves ──────────────────
  -- Y1: Feijão + Macarrão = 14,70   PIX
  ('c1000000-0000-0000-0000-000000000029'::uuid,0,'b0000000-0000-0000-0000-000000000007','COMPLETED',NULL, 14.70, 14.70, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Y2: Leite + Café + Açúcar = 15,30   CASH   20,00 (troco 4,70)
  ('c1000000-0000-0000-0000-000000000030'::uuid,0,'b0000000-0000-0000-0000-000000000007','COMPLETED',NULL, 15.30, 15.30, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Y3: Refrigerante 2L + Suco Uva = 19,50   CREDIT_CARD
  ('c1000000-0000-0000-0000-000000000031'::uuid,0,'b0000000-0000-0000-0000-000000000007','COMPLETED',NULL, 19.50, 19.50, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Y4: 2×Biscoito + Achocolatado = 11,90   DEBIT_CARD
  ('c1000000-0000-0000-0000-000000000032'::uuid,0,'b0000000-0000-0000-0000-000000000007','COMPLETED',NULL, 11.90, 11.90, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),

  -- ── b008 · 07/03 · CAIXA-01 · Pedro Costa — dia de pico ─────
  -- Z1: 2×Arroz + Feijão + Óleo = 64,10   PIX
  ('c1000000-0000-0000-0000-000000000033'::uuid,0,'b0000000-0000-0000-0000-000000000008','COMPLETED',NULL, 64.10, 64.10, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Z2: 3×Coca + 2×Salgadinho + Chocolate = 30,90   CASH   35,00 (troco 4,10)
  ('c1000000-0000-0000-0000-000000000034'::uuid,0,'b0000000-0000-0000-0000-000000000008','COMPLETED',NULL, 30.90, 30.90, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Z3: Shampoo + Sabonete + Detergente = 18,90   CREDIT_CARD
  ('c1000000-0000-0000-0000-000000000035'::uuid,0,'b0000000-0000-0000-0000-000000000008','COMPLETED',NULL, 18.90, 18.90, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Z4: Papel Higiênico + Amaciante + Desinfetante = 32,20   DEBIT_CARD
  ('c1000000-0000-0000-0000-000000000036'::uuid,0,'b0000000-0000-0000-0000-000000000008','COMPLETED',NULL, 32.20, 32.20, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Z5: 2×Pão + Manteiga + Café = 30,70   PIX
  ('c1000000-0000-0000-0000-000000000037'::uuid,0,'b0000000-0000-0000-0000-000000000008','COMPLETED',NULL, 30.70, 30.70, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- Z6: Cereal + Leite (cancelada)
  ('c1000000-0000-0000-0000-000000000038'::uuid,0,'b0000000-0000-0000-0000-000000000008','CANCELED', NULL, 14.40, 14.40,14.40,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),

  -- ── b009 · 08/03 · CAIXA-02 · João Silva ────────────────────
  -- AA1: Suco Uva + 2×Achocolatado = 16,80   CASH   20,00 (troco 3,20)
  ('c1000000-0000-0000-0000-000000000039'::uuid,0,'b0000000-0000-0000-0000-000000000009','COMPLETED',NULL, 16.80, 16.80, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- AA2: Macarrão + Creme de Leite + Fermento = 12,20   PIX
  ('c1000000-0000-0000-0000-000000000040'::uuid,0,'b0000000-0000-0000-0000-000000000009','COMPLETED',NULL, 12.20, 12.20, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- AA3: Arroz + Manteiga = 31,80   DEBIT_CARD
  ('c1000000-0000-0000-0000-000000000041'::uuid,0,'b0000000-0000-0000-0000-000000000009','COMPLETED',NULL, 31.80, 31.80, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- AA4: Amaciante (cancelada)
  ('c1000000-0000-0000-0000-000000000042'::uuid,0,'b0000000-0000-0000-0000-000000000009','CANCELED', NULL, 15.90, 15.90,15.90,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),

  -- ── b010 · 09/03 · CAIXA-01 · Maria Santos ──────────────────
  -- BB1: 2×Refrigerante 2L + Suco Laranja = 24,00   PIX
  ('c1000000-0000-0000-0000-000000000043'::uuid,0,'b0000000-0000-0000-0000-000000000010','COMPLETED',NULL, 24.00, 24.00, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- BB2: Arroz + Feijão + Óleo + Macarrão = 45,40   CASH   50,00 (troco 4,60)
  ('c1000000-0000-0000-0000-000000000044'::uuid,0,'b0000000-0000-0000-0000-000000000010','COMPLETED',NULL, 45.40, 45.40, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- BB3: Leite + 2×Iogurte + Manteiga = 19,80   CREDIT_CARD
  ('c1000000-0000-0000-0000-000000000045'::uuid,0,'b0000000-0000-0000-0000-000000000010','COMPLETED',NULL, 19.80, 19.80, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- BB4: Shampoo + Papel Higiênico + Sabonete = 27,30   DEBIT_CARD
  ('c1000000-0000-0000-0000-000000000046'::uuid,0,'b0000000-0000-0000-0000-000000000010','COMPLETED',NULL, 27.30, 27.30, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- BB5: Chocolate (cancelada)
  ('c1000000-0000-0000-0000-000000000047'::uuid,0,'b0000000-0000-0000-0000-000000000010','CANCELED', NULL,  6.90,  6.90, 6.90,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),

  -- ── b011 · 09/03 · CAIXA-02 · Rafael Alves ──────────────────
  -- CC1: Cereal + Leite + Café = 21,20   PIX
  ('c1000000-0000-0000-0000-000000000048'::uuid,0,'b0000000-0000-0000-0000-000000000011','COMPLETED',NULL, 21.20, 21.20, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- CC2: Vinagre + Creme de Leite + Fermento = 11,10   DEBIT_CARD
  ('c1000000-0000-0000-0000-000000000049'::uuid,0,'b0000000-0000-0000-0000-000000000011','COMPLETED',NULL, 11.10, 11.10, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid),
  -- CC3: 2×Açúcar + Detergente + Desinfetante = 15,90   CASH   20,00 (troco 4,10)
  ('c1000000-0000-0000-0000-000000000050'::uuid,0,'b0000000-0000-0000-0000-000000000011','COMPLETED',NULL, 15.90, 15.90, 0.00,0.00,0,'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 23. ITENS DAS VENDAS HISTÓRICAS (d2...001 a d2...082)
-- ---------------------------------------------------------------
INSERT INTO sale_item (id, sale_id, product_id, quantity, unit_price, discount)
SELECT i.item_id::uuid, i.sale_id::uuid, p.id, i.qty, i.price::numeric(19,2), 0.00
FROM (VALUES
  -- ── b004 ─────────────────────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000001','c1000000-0000-0000-0000-000000000016','PRD-001',3,5.50),  -- P
  ('d2000000-0000-0000-0000-000000000002','c1000000-0000-0000-0000-000000000016','PRD-003',2,3.75),
  ('d2000000-0000-0000-0000-000000000003','c1000000-0000-0000-0000-000000000017','PRD-011',1,4.50),  -- Q
  ('d2000000-0000-0000-0000-000000000004','c1000000-0000-0000-0000-000000000017','PRD-014',1,7.50),
  ('d2000000-0000-0000-0000-000000000005','c1000000-0000-0000-0000-000000000017','PRD-012',1,8.90),
  ('d2000000-0000-0000-0000-000000000006','c1000000-0000-0000-0000-000000000018','PRD-007',1,22.90), -- R
  ('d2000000-0000-0000-0000-000000000007','c1000000-0000-0000-0000-000000000018','PRD-008',1,10.50),
  ('d2000000-0000-0000-0000-000000000008','c1000000-0000-0000-0000-000000000019','PRD-002',2,2.00),  -- S
  ('d2000000-0000-0000-0000-000000000009','c1000000-0000-0000-0000-000000000019','PRD-005',1,4.50),
  ('d2000000-0000-0000-0000-000000000010','c1000000-0000-0000-0000-000000000020','PRD-004',1,6.90),  -- T (cancelada)
  -- ── b005 ─────────────────────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000011','c1000000-0000-0000-0000-000000000021','PRD-018',1,12.90), -- U
  ('d2000000-0000-0000-0000-000000000012','c1000000-0000-0000-0000-000000000021','PRD-019',1,2.50),
  ('d2000000-0000-0000-0000-000000000013','c1000000-0000-0000-0000-000000000022','PRD-020',1,10.90), -- V
  ('d2000000-0000-0000-0000-000000000014','c1000000-0000-0000-0000-000000000022','PRD-021',1,15.90),
  ('d2000000-0000-0000-0000-000000000015','c1000000-0000-0000-0000-000000000023','PRD-025',1,9.90),  -- W
  ('d2000000-0000-0000-0000-000000000016','c1000000-0000-0000-0000-000000000023','PRD-011',1,4.50),
  -- ── b006 ─────────────────────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000017','c1000000-0000-0000-0000-000000000024','PRD-006',2,7.00),  -- X1
  ('d2000000-0000-0000-0000-000000000018','c1000000-0000-0000-0000-000000000024','PRD-004',1,6.90),
  ('d2000000-0000-0000-0000-000000000019','c1000000-0000-0000-0000-000000000025','PRD-007',1,22.90), -- X2
  ('d2000000-0000-0000-0000-000000000020','c1000000-0000-0000-0000-000000000025','PRD-009',1,7.80),
  ('d2000000-0000-0000-0000-000000000021','c1000000-0000-0000-0000-000000000026','PRD-017',1,3.50),  -- X3
  ('d2000000-0000-0000-0000-000000000022','c1000000-0000-0000-0000-000000000026','PRD-019',1,2.50),
  ('d2000000-0000-0000-0000-000000000023','c1000000-0000-0000-0000-000000000026','PRD-022',1,5.40),
  ('d2000000-0000-0000-0000-000000000024','c1000000-0000-0000-0000-000000000027','PRD-013',2,3.20),  -- X4
  ('d2000000-0000-0000-0000-000000000025','c1000000-0000-0000-0000-000000000027','PRD-012',1,8.90),
  ('d2000000-0000-0000-0000-000000000026','c1000000-0000-0000-0000-000000000028','PRD-021',1,15.90), -- X5 (canc.)
  -- ── b007 ─────────────────────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000027','c1000000-0000-0000-0000-000000000029','PRD-008',1,10.50), -- Y1
  ('d2000000-0000-0000-0000-000000000028','c1000000-0000-0000-0000-000000000029','PRD-010',1,4.20),
  ('d2000000-0000-0000-0000-000000000029','c1000000-0000-0000-0000-000000000030','PRD-011',1,4.50),  -- Y2
  ('d2000000-0000-0000-0000-000000000030','c1000000-0000-0000-0000-000000000030','PRD-015',1,6.80),
  ('d2000000-0000-0000-0000-000000000031','c1000000-0000-0000-0000-000000000030','PRD-016',1,4.00),
  ('d2000000-0000-0000-0000-000000000032','c1000000-0000-0000-0000-000000000031','PRD-023',1,8.50),  -- Y3
  ('d2000000-0000-0000-0000-000000000033','c1000000-0000-0000-0000-000000000031','PRD-024',1,11.00),
  ('d2000000-0000-0000-0000-000000000034','c1000000-0000-0000-0000-000000000032','PRD-005',2,4.50),  -- Y4
  ('d2000000-0000-0000-0000-000000000035','c1000000-0000-0000-0000-000000000032','PRD-030',1,2.90),
  -- ── b008 — dia de pico ───────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000036','c1000000-0000-0000-0000-000000000033','PRD-007',2,22.90), -- Z1
  ('d2000000-0000-0000-0000-000000000037','c1000000-0000-0000-0000-000000000033','PRD-008',1,10.50),
  ('d2000000-0000-0000-0000-000000000038','c1000000-0000-0000-0000-000000000033','PRD-009',1,7.80),
  ('d2000000-0000-0000-0000-000000000039','c1000000-0000-0000-0000-000000000034','PRD-001',3,5.50),  -- Z2
  ('d2000000-0000-0000-0000-000000000040','c1000000-0000-0000-0000-000000000034','PRD-003',2,3.75),
  ('d2000000-0000-0000-0000-000000000041','c1000000-0000-0000-0000-000000000034','PRD-004',1,6.90),
  ('d2000000-0000-0000-0000-000000000042','c1000000-0000-0000-0000-000000000035','PRD-018',1,12.90), -- Z3
  ('d2000000-0000-0000-0000-000000000043','c1000000-0000-0000-0000-000000000035','PRD-017',1,3.50),
  ('d2000000-0000-0000-0000-000000000044','c1000000-0000-0000-0000-000000000035','PRD-019',1,2.50),
  ('d2000000-0000-0000-0000-000000000045','c1000000-0000-0000-0000-000000000036','PRD-020',1,10.90), -- Z4
  ('d2000000-0000-0000-0000-000000000046','c1000000-0000-0000-0000-000000000036','PRD-021',1,15.90),
  ('d2000000-0000-0000-0000-000000000047','c1000000-0000-0000-0000-000000000036','PRD-022',1,5.40),
  ('d2000000-0000-0000-0000-000000000048','c1000000-0000-0000-0000-000000000037','PRD-014',2,7.50),  -- Z5
  ('d2000000-0000-0000-0000-000000000049','c1000000-0000-0000-0000-000000000037','PRD-012',1,8.90),
  ('d2000000-0000-0000-0000-000000000050','c1000000-0000-0000-0000-000000000037','PRD-015',1,6.80),
  ('d2000000-0000-0000-0000-000000000051','c1000000-0000-0000-0000-000000000038','PRD-025',1,9.90),  -- Z6 (canc.)
  ('d2000000-0000-0000-0000-000000000052','c1000000-0000-0000-0000-000000000038','PRD-011',1,4.50),
  -- ── b009 ─────────────────────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000053','c1000000-0000-0000-0000-000000000039','PRD-024',1,11.00), -- AA1
  ('d2000000-0000-0000-0000-000000000054','c1000000-0000-0000-0000-000000000039','PRD-030',2,2.90),
  ('d2000000-0000-0000-0000-000000000055','c1000000-0000-0000-0000-000000000040','PRD-010',1,4.20),  -- AA2
  ('d2000000-0000-0000-0000-000000000056','c1000000-0000-0000-0000-000000000040','PRD-028',1,5.20),
  ('d2000000-0000-0000-0000-000000000057','c1000000-0000-0000-0000-000000000040','PRD-026',1,2.80),
  ('d2000000-0000-0000-0000-000000000058','c1000000-0000-0000-0000-000000000041','PRD-007',1,22.90), -- AA3
  ('d2000000-0000-0000-0000-000000000059','c1000000-0000-0000-0000-000000000041','PRD-012',1,8.90),
  ('d2000000-0000-0000-0000-000000000060','c1000000-0000-0000-0000-000000000042','PRD-021',1,15.90), -- AA4 (canc.)
  -- ── b010 ─────────────────────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000061','c1000000-0000-0000-0000-000000000043','PRD-023',2,8.50),  -- BB1
  ('d2000000-0000-0000-0000-000000000062','c1000000-0000-0000-0000-000000000043','PRD-006',1,7.00),
  ('d2000000-0000-0000-0000-000000000063','c1000000-0000-0000-0000-000000000044','PRD-007',1,22.90), -- BB2
  ('d2000000-0000-0000-0000-000000000064','c1000000-0000-0000-0000-000000000044','PRD-008',1,10.50),
  ('d2000000-0000-0000-0000-000000000065','c1000000-0000-0000-0000-000000000044','PRD-009',1,7.80),
  ('d2000000-0000-0000-0000-000000000066','c1000000-0000-0000-0000-000000000044','PRD-010',1,4.20),
  ('d2000000-0000-0000-0000-000000000067','c1000000-0000-0000-0000-000000000045','PRD-011',1,4.50),  -- BB3
  ('d2000000-0000-0000-0000-000000000068','c1000000-0000-0000-0000-000000000045','PRD-013',2,3.20),
  ('d2000000-0000-0000-0000-000000000069','c1000000-0000-0000-0000-000000000045','PRD-012',1,8.90),
  ('d2000000-0000-0000-0000-000000000070','c1000000-0000-0000-0000-000000000046','PRD-018',1,12.90), -- BB4
  ('d2000000-0000-0000-0000-000000000071','c1000000-0000-0000-0000-000000000046','PRD-020',1,10.90),
  ('d2000000-0000-0000-0000-000000000072','c1000000-0000-0000-0000-000000000046','PRD-017',1,3.50),
  ('d2000000-0000-0000-0000-000000000073','c1000000-0000-0000-0000-000000000047','PRD-004',1,6.90),  -- BB5 (canc.)
  -- ── b011 ─────────────────────────────────────────────────────
  ('d2000000-0000-0000-0000-000000000074','c1000000-0000-0000-0000-000000000048','PRD-025',1,9.90),  -- CC1
  ('d2000000-0000-0000-0000-000000000075','c1000000-0000-0000-0000-000000000048','PRD-011',1,4.50),
  ('d2000000-0000-0000-0000-000000000076','c1000000-0000-0000-0000-000000000048','PRD-015',1,6.80),
  ('d2000000-0000-0000-0000-000000000077','c1000000-0000-0000-0000-000000000049','PRD-027',1,3.10),  -- CC2
  ('d2000000-0000-0000-0000-000000000078','c1000000-0000-0000-0000-000000000049','PRD-028',1,5.20),
  ('d2000000-0000-0000-0000-000000000079','c1000000-0000-0000-0000-000000000049','PRD-026',1,2.80),
  ('d2000000-0000-0000-0000-000000000080','c1000000-0000-0000-0000-000000000050','PRD-016',2,4.00),  -- CC3
  ('d2000000-0000-0000-0000-000000000081','c1000000-0000-0000-0000-000000000050','PRD-019',1,2.50),
  ('d2000000-0000-0000-0000-000000000082','c1000000-0000-0000-0000-000000000050','PRD-022',1,5.40)
) AS i(item_id, sale_id, internal_code, qty, price)
JOIN product p ON p.internal_code = i.internal_code
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 24. PAGAMENTOS DAS VENDAS HISTÓRICAS (e2...001 a e2...030)
--     Apenas vendas COMPLETED recebem pagamento.
-- ---------------------------------------------------------------
INSERT INTO payment (id, sale_id, method, amount, change_amount, transaction_id, confirmed, created_at, updated_at)
VALUES
  -- ── b004 · 03/03 ────────────────────────────────────────────
  ('e2000000-0000-0000-0000-000000000001','c1000000-0000-0000-0000-000000000016'::uuid,'CASH',        25.00, 1.00, NULL,           true,'2026-03-03 09:30:00','2026-03-03 09:30:00'),
  ('e2000000-0000-0000-0000-000000000002','c1000000-0000-0000-0000-000000000017'::uuid,'PIX',         20.90, 0.00,'TXN-PIX-004',  true,'2026-03-03 10:15:00','2026-03-03 10:15:00'),
  ('e2000000-0000-0000-0000-000000000003','c1000000-0000-0000-0000-000000000018'::uuid,'CREDIT_CARD', 33.40, 0.00,'TXN-CC-003',   true,'2026-03-03 11:00:00','2026-03-03 11:00:00'),
  ('e2000000-0000-0000-0000-000000000004','c1000000-0000-0000-0000-000000000019'::uuid,'DEBIT_CARD',   8.50, 0.00,'TXN-DB-004',   true,'2026-03-03 14:30:00','2026-03-03 14:30:00'),
  -- ── b005 · 03/03 ────────────────────────────────────────────
  ('e2000000-0000-0000-0000-000000000005','c1000000-0000-0000-0000-000000000021'::uuid,'PIX',         15.40, 0.00,'TXN-PIX-005',  true,'2026-03-03 10:00:00','2026-03-03 10:00:00'),
  ('e2000000-0000-0000-0000-000000000006','c1000000-0000-0000-0000-000000000022'::uuid,'CREDIT_CARD', 26.80, 0.00,'TXN-CC-004',   true,'2026-03-03 13:00:00','2026-03-03 13:00:00'),
  ('e2000000-0000-0000-0000-000000000007','c1000000-0000-0000-0000-000000000023'::uuid,'CASH',        15.00, 0.60, NULL,           true,'2026-03-03 16:00:00','2026-03-03 16:00:00'),
  -- ── b006 · 04/03 ────────────────────────────────────────────
  ('e2000000-0000-0000-0000-000000000008','c1000000-0000-0000-0000-000000000024'::uuid,'PIX',         20.90, 0.00,'TXN-PIX-006',  true,'2026-03-04 09:00:00','2026-03-04 09:00:00'),
  ('e2000000-0000-0000-0000-000000000009','c1000000-0000-0000-0000-000000000025'::uuid,'CASH',        35.00, 4.30, NULL,           true,'2026-03-04 11:30:00','2026-03-04 11:30:00'),
  ('e2000000-0000-0000-0000-000000000010','c1000000-0000-0000-0000-000000000026'::uuid,'DEBIT_CARD',  11.40, 0.00,'TXN-DB-005',   true,'2026-03-04 13:00:00','2026-03-04 13:00:00'),
  ('e2000000-0000-0000-0000-000000000011','c1000000-0000-0000-0000-000000000027'::uuid,'CREDIT_CARD', 15.30, 0.00,'TXN-CC-005',   true,'2026-03-04 16:00:00','2026-03-04 16:00:00'),
  -- ── b007 · 05/03 ────────────────────────────────────────────
  ('e2000000-0000-0000-0000-000000000012','c1000000-0000-0000-0000-000000000029'::uuid,'PIX',         14.70, 0.00,'TXN-PIX-007',  true,'2026-03-05 10:00:00','2026-03-05 10:00:00'),
  ('e2000000-0000-0000-0000-000000000013','c1000000-0000-0000-0000-000000000030'::uuid,'CASH',        20.00, 4.70, NULL,           true,'2026-03-05 12:00:00','2026-03-05 12:00:00'),
  ('e2000000-0000-0000-0000-000000000014','c1000000-0000-0000-0000-000000000031'::uuid,'CREDIT_CARD', 19.50, 0.00,'TXN-CC-006',   true,'2026-03-05 14:30:00','2026-03-05 14:30:00'),
  ('e2000000-0000-0000-0000-000000000015','c1000000-0000-0000-0000-000000000032'::uuid,'DEBIT_CARD',  11.90, 0.00,'TXN-DB-006',   true,'2026-03-05 17:00:00','2026-03-05 17:00:00'),
  -- ── b008 · 07/03 — dia de pico ──────────────────────────────
  ('e2000000-0000-0000-0000-000000000016','c1000000-0000-0000-0000-000000000033'::uuid,'PIX',         64.10, 0.00,'TXN-PIX-008',  true,'2026-03-07 09:00:00','2026-03-07 09:00:00'),
  ('e2000000-0000-0000-0000-000000000017','c1000000-0000-0000-0000-000000000034'::uuid,'CASH',        35.00, 4.10, NULL,           true,'2026-03-07 11:00:00','2026-03-07 11:00:00'),
  ('e2000000-0000-0000-0000-000000000018','c1000000-0000-0000-0000-000000000035'::uuid,'CREDIT_CARD', 18.90, 0.00,'TXN-CC-007',   true,'2026-03-07 13:30:00','2026-03-07 13:30:00'),
  ('e2000000-0000-0000-0000-000000000019','c1000000-0000-0000-0000-000000000036'::uuid,'DEBIT_CARD',  32.20, 0.00,'TXN-DB-007',   true,'2026-03-07 15:00:00','2026-03-07 15:00:00'),
  ('e2000000-0000-0000-0000-000000000020','c1000000-0000-0000-0000-000000000037'::uuid,'PIX',         30.70, 0.00,'TXN-PIX-009',  true,'2026-03-07 17:00:00','2026-03-07 17:00:00'),
  -- ── b009 · 08/03 ────────────────────────────────────────────
  ('e2000000-0000-0000-0000-000000000021','c1000000-0000-0000-0000-000000000039'::uuid,'CASH',        20.00, 3.20, NULL,           true,'2026-03-08 09:30:00','2026-03-08 09:30:00'),
  ('e2000000-0000-0000-0000-000000000022','c1000000-0000-0000-0000-000000000040'::uuid,'PIX',         12.20, 0.00,'TXN-PIX-010',  true,'2026-03-08 11:00:00','2026-03-08 11:00:00'),
  ('e2000000-0000-0000-0000-000000000023','c1000000-0000-0000-0000-000000000041'::uuid,'DEBIT_CARD',  31.80, 0.00,'TXN-DB-008',   true,'2026-03-08 14:00:00','2026-03-08 14:00:00'),
  -- ── b010 · 09/03 — CAIXA-01 ────────────────────────────────
  ('e2000000-0000-0000-0000-000000000024','c1000000-0000-0000-0000-000000000043'::uuid,'PIX',         24.00, 0.00,'TXN-PIX-011',  true,'2026-03-09 09:00:00','2026-03-09 09:00:00'),
  ('e2000000-0000-0000-0000-000000000025','c1000000-0000-0000-0000-000000000044'::uuid,'CASH',        50.00, 4.60, NULL,           true,'2026-03-09 11:00:00','2026-03-09 11:00:00'),
  ('e2000000-0000-0000-0000-000000000026','c1000000-0000-0000-0000-000000000045'::uuid,'CREDIT_CARD', 19.80, 0.00,'TXN-CC-008',   true,'2026-03-09 13:30:00','2026-03-09 13:30:00'),
  ('e2000000-0000-0000-0000-000000000027','c1000000-0000-0000-0000-000000000046'::uuid,'DEBIT_CARD',  27.30, 0.00,'TXN-DB-009',   true,'2026-03-09 16:00:00','2026-03-09 16:00:00'),
  -- ── b011 · 09/03 — CAIXA-02 ────────────────────────────────
  ('e2000000-0000-0000-0000-000000000028','c1000000-0000-0000-0000-000000000048'::uuid,'PIX',         21.20, 0.00,'TXN-PIX-012',  true,'2026-03-09 10:30:00','2026-03-09 10:30:00'),
  ('e2000000-0000-0000-0000-000000000029','c1000000-0000-0000-0000-000000000049'::uuid,'DEBIT_CARD',  11.10, 0.00,'TXN-DB-010',   true,'2026-03-09 13:00:00','2026-03-09 13:00:00'),
  ('e2000000-0000-0000-0000-000000000030','c1000000-0000-0000-0000-000000000050'::uuid,'CASH',        20.00, 4.10, NULL,           true,'2026-03-09 16:00:00','2026-03-09 16:00:00')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 24.1 CONTAS WEB DE TESTE
--     Senha para todas as contas abaixo: 123456
--     Observação: para facilitar os testes do fluxo web de suporte,
--     as contas já entram verificadas.
-- ---------------------------------------------------------------
INSERT INTO account (id, tenant_id, company_id, name, email, password_hash, role, verified, created_at)
VALUES
  ('d1000000-0000-0000-0000-000000000001'::uuid, '123e4567-e89b-12d3-a456-426614174000'::uuid, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid, 'Administrador', 'admin@sistema.local', '$2a$10$gGUGvOEla0U759O0Jbw4cu/mr3tlQFvOPYapX/TlvkPH4S4ikNu.i', 'ADMIN', true, '2026-03-01 08:00:00'),
  ('d1000000-0000-0000-0000-000000000002'::uuid, '123e4567-e89b-12d3-a456-426614174000'::uuid, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid, 'João Silva', 'joao.silva@empresa.com', '$2a$10$gGUGvOEla0U759O0Jbw4cu/mr3tlQFvOPYapX/TlvkPH4S4ikNu.i', 'ADMIN', true, '2026-03-01 08:05:00'),
  ('d1000000-0000-0000-0000-000000000003'::uuid, '123e4567-e89b-12d3-a456-426614174000'::uuid, 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'::uuid, 'Pedro Costa', 'pedro.costa@empresa.com', '$2a$10$gGUGvOEla0U759O0Jbw4cu/mr3tlQFvOPYapX/TlvkPH4S4ikNu.i', 'ADMIN', true, '2026-03-01 08:10:00')
ON CONFLICT (email) DO UPDATE SET company_id = EXCLUDED.company_id;

-- ===============================================================
-- BLOCO III — SUPORTE: AGENTES, USUÁRIOS E CHAMADOS
-- ---------------------------------------------------------------
-- Cobre todas as telas de suporte:
--   • Lista de chamados com diferentes status e prioridades
--   • Detalhe do chamado com histórico de interações
--   • Agentes ativos e inativos
-- ===============================================================

-- ---------------------------------------------------------------
-- 25. AGENTES DE SUPORTE
-- ---------------------------------------------------------------
INSERT INTO support.agents (id, employee_id, name, active)
VALUES
  ('f2000000-0000-0000-0000-000000000001'::uuid, 'AGT-001', 'Carlos Anderson Silva',  true),
  ('f2000000-0000-0000-0000-000000000002'::uuid, 'AGT-002', 'Fernanda Lima Alves',    true),
  ('f2000000-0000-0000-0000-000000000003'::uuid, 'AGT-003', 'Roberto Nunes Santos',   false)
ON CONFLICT (employee_id) DO NOTHING;

-- ---------------------------------------------------------------
-- 26. USUÁRIOS DE SUPORTE
--     admin@sistema.local → fallback automático do backend quando
--     o chamado é aberto sem nome/email (novo comportamento).
-- ---------------------------------------------------------------
INSERT INTO support.users (email, name)
VALUES
  ('admin@sistema.local',       'Administrador'),
  ('joao.silva@empresa.com',    'João Silva'),
  ('pedro.costa@empresa.com',   'Pedro Costa')
ON CONFLICT (email) DO NOTHING;

-- ---------------------------------------------------------------
-- 27. CHAMADOS DE SUPORTE — todos os status e prioridades
-- ---------------------------------------------------------------
INSERT INTO support.tickets
  (id, title, description, status, priority,
   user_id, agent_id, category_id,
   sla_active, sla_started_at, version, created_at, updated_at)
SELECT
  t.ticket_id::uuid,
  t.title,
  t.description,
  t.status,
  t.priority,
  u.id,
  a.id,
  cat.id,
  t.sla_active,
  t.sla_started_at::timestamp,
  0,
  t.created_at::timestamp,
  t.updated_at::timestamp
FROM (VALUES
  -- OPEN · HIGH · System/Bug — nenhum agente ainda
  ('f3000000-0000-0000-0000-000000000001',
   'Caixa trava ao processar pagamento via PIX',
   'O CAIXA-01 congela toda vez que o operador tenta confirmar um pagamento via PIX acima de R$50,00. O sistema exibe a mensagem "processando" indefinidamente e o único recurso é reiniciar o processo.',
   'OPEN','HIGH','admin@sistema.local',NULL,'System','Bug',
   true,'2026-03-09 14:20:00','2026-03-09 14:20:00','2026-03-09 14:20:00'),
  -- OPEN · LOW · Account/Password Reset
  ('f3000000-0000-0000-0000-000000000002',
   'Redefinição de senha do operador Lucas Oliveira (OP-004)',
   'Lucas está sem acesso desde ontem. Solicito redefinição da senha de acesso ao PDV.',
   'OPEN','LOW','admin@sistema.local',NULL,'Account','Password Reset',
   true,'2026-03-09 16:05:00','2026-03-09 16:05:00','2026-03-09 16:05:00'),
  -- IN_PROGRESS · HIGH · Account/Access
  ('f3000000-0000-0000-0000-000000000003',
   'Operador não consegue acessar o módulo administrativo após atualização',
   'Após a atualização do sistema na semana passada, o operador Maria Santos (OP-002) não consegue mais visualizar a aba "Relatórios". A tela exibe "Acesso negado" mesmo com permissão SUPERVISOR.',
   'IN_PROGRESS','HIGH','admin@sistema.local','AGT-001','Account','Access',
   true,'2026-03-07 09:30:00','2026-03-07 09:30:00','2026-03-08 11:00:00'),
  -- IN_PROGRESS · HIGH · Billing/Charge Dispute
  ('f3000000-0000-0000-0000-000000000004',
   'Cobrança duplicada identificada na sessão de 06/03/2026',
   'Durante a conferência do fechamento do caixa CAIXA-02 do dia 06/03, identifiquei que a Venda C (PIX R$33,40) foi registrada duas vezes no sistema, mas o cliente pagou apenas uma vez.',
   'IN_PROGRESS','HIGH','pedro.costa@empresa.com','AGT-001','Billing','Charge Dispute',
   true,'2026-03-08 10:15:00','2026-03-08 10:15:00','2026-03-09 09:00:00'),
  -- WAITING_FOR_CUSTOMER · LOW · General/Question
  ('f3000000-0000-0000-0000-000000000005',
   'Dúvida sobre configuração do programa de fidelidade',
   'Gostaria de entender como configurar uma nova política de fidelidade com meta de 200 pontos e desconto de R$30,00. Qual o processo correto?',
   'WAITING_FOR_CUSTOMER','LOW','joao.silva@empresa.com','AGT-002','General','Question',
   false,'2026-03-05 14:00:00','2026-03-05 14:00:00','2026-03-06 16:30:00'),
  -- RESOLVED · MEDIUM · Billing/Invoice
  ('f3000000-0000-0000-0000-000000000006',
   'Relatório financeiro exibindo valores inconsistentes',
   'O relatório de fechamento do dia 04/03 está exibindo total de R$0,00 no gráfico de distribuição por pagamento, porém os valores individuais estão corretos.',
   'RESOLVED','MEDIUM','admin@sistema.local','AGT-002','Billing','Invoice',
   false,'2026-03-05 08:00:00','2026-03-05 08:00:00','2026-03-06 12:00:00'),
  -- RESOLVED · LOW · General/Feedback
  ('f3000000-0000-0000-0000-000000000007',
   'Sugestão: exportação de relatórios em PDF',
   'Seria muito útil poder exportar os relatórios de fechamento de caixa em PDF para arquivamento mensal. Hoje precisamos fazer print screen.',
   'RESOLVED','LOW','joao.silva@empresa.com','AGT-002','General','Feedback',
   false,'2026-03-03 11:00:00','2026-03-03 11:00:00','2026-03-04 15:00:00'),
  -- CLOSED · MEDIUM · System/Performance
  ('f3000000-0000-0000-0000-000000000008',
   'Sistema com lentidão no horário de pico (12h–14h)',
   'Todos os dias entre 12h e 14h o sistema fica muito lento no PDV. O carregamento dos produtos demora mais de 5 segundos.',
   'CLOSED','MEDIUM','pedro.costa@empresa.com','AGT-003','System','Performance',
   false,'2026-02-20 09:00:00','2026-02-20 09:00:00','2026-03-01 10:00:00'),
  -- IN_PROGRESS · General/Question · última mensagem pública do cliente
  ('f3000000-0000-0000-0000-000000000009',
   'Confirmação de uso do atalho de fechamento',
   'Depois da atualização mais recente, quero confirmar se o atalho Ctrl+Shift+F continua sendo a forma correta de abrir o fechamento de caixa no desktop.',
   'IN_PROGRESS','MEDIUM','joao.silva@empresa.com','AGT-002','General','Question',
   true,'2026-03-10 09:00:00','2026-03-10 09:00:00','2026-03-10 10:20:00'),
  -- RESOLVED · General/Feedback · última mensagem pública do atendente
  ('f3000000-0000-0000-0000-000000000010',
   'Sugestão de atalho para reimprimir comprovante',
   'Seria ótimo ter um atalho direto no teclado para reimpressão do comprovante de venda sem precisar abrir o menu de contexto.',
   'RESOLVED','LOW','pedro.costa@empresa.com','AGT-001','General','Feedback',
   false,'2026-03-10 11:00:00','2026-03-10 11:00:00','2026-03-10 12:15:00')
) AS t(ticket_id, title, description, status, priority,
       user_email, agent_employee_id,
       category_name, category_subcategory,
       sla_active, sla_started_at, created_at, updated_at)
JOIN support.users   u   ON u.email       = t.user_email
LEFT JOIN support.agents a ON a.employee_id = t.agent_employee_id
JOIN support.categories cat ON cat.name = t.category_name
                           AND cat.subcategory = t.category_subcategory
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 28. INTERAÇÕES DOS CHAMADOS
--     Simula o histórico de mensagens para testar a tela de detalhe.
-- ---------------------------------------------------------------
INSERT INTO support.interactions (id, ticket_id, content, type, created_at)
VALUES
  -- Ticket 1 (OPEN · Bug PIX)
  ('f4000000-0000-0000-0000-000000000001'::uuid,
   'f3000000-0000-0000-0000-000000000001'::uuid,
   'Problema confirmado: aconteceu novamente às 14h15 com o operador João Silva ao tentar receber R$64,10 via PIX na sessão de 07/03. Prints anexados.',
   'CUSTOMER_MESSAGE','2026-03-09 14:20:00'),

  -- Ticket 3 (IN_PROGRESS · Acesso negado)
  ('f4000000-0000-0000-0000-000000000002'::uuid,
   'f3000000-0000-0000-0000-000000000003'::uuid,
   'Desde a atualização do dia 06/03 não consigo mais acessar os relatórios. Tentei no Chrome e no Edge, mesmo erro.',
   'CUSTOMER_MESSAGE','2026-03-07 09:30:00'),
  ('f4000000-0000-0000-0000-000000000003'::uuid,
   'f3000000-0000-0000-0000-000000000003'::uuid,
   'Olá! Já identificamos o problema — um ajuste de permissões na última deploy afetou o nível SUPERVISOR. Estamos preparando a correção e aplicaremos ainda hoje.',
   'AGENT_MESSAGE','2026-03-07 11:00:00'),
  ('f4000000-0000-0000-0000-000000000004'::uuid,
   'f3000000-0000-0000-0000-000000000003'::uuid,
   'Aguardando confirmação do cliente após aplicação do hotfix #2847.',
   'INTERNAL_NOTE','2026-03-08 11:00:00'),

  -- Ticket 4 (IN_PROGRESS · Cobrança duplicada)
  ('f4000000-0000-0000-0000-000000000005'::uuid,
   'f3000000-0000-0000-0000-000000000004'::uuid,
   'Confirmo a duplicidade: sale_id c1000000-...-000000000003 aparece duas vezes no movimento financeiro do dia 06/03.',
   'CUSTOMER_MESSAGE','2026-03-08 10:15:00'),
  ('f4000000-0000-0000-0000-000000000006'::uuid,
   'f3000000-0000-0000-0000-000000000004'::uuid,
   'Análise iniciada. Verificamos os logs de pagamento — o registro duplicado parece ter ocorrido por timeout de rede com reenvio automático da notificação PIX. Estamos abrindo ticket interno com a equipe de infra.',
   'AGENT_MESSAGE','2026-03-09 09:00:00'),

  -- Ticket 5 (WAITING_FOR_CUSTOMER · Fidelidade)
  ('f4000000-0000-0000-0000-000000000007'::uuid,
   'f3000000-0000-0000-0000-000000000005'::uuid,
   'Preciso configurar uma nova política: 200 pontos = R$30 de desconto. Como faço?',
   'CUSTOMER_MESSAGE','2026-03-05 14:00:00'),
  ('f4000000-0000-0000-0000-000000000008'::uuid,
   'f3000000-0000-0000-0000-000000000005'::uuid,
   'Bom dia! Para criar uma nova política acesse Admin → Fidelidade → Nova Política. Preencha: Pontos necessários = 200, Desconto = R$30,00, Valor por ponto = R$1,00. Confirme se conseguiu criar e se tem alguma dúvida adicional.',
   'AGENT_MESSAGE','2026-03-06 09:00:00'),

  -- Ticket 6 (RESOLVED · Relatório)
  ('f4000000-0000-0000-0000-000000000009'::uuid,
   'f3000000-0000-0000-0000-000000000006'::uuid,
   'O gráfico de pagamentos mostra R$0,00 no total mas os detalhes estão corretos.',
   'CUSTOMER_MESSAGE','2026-03-05 08:00:00'),
  ('f4000000-0000-0000-0000-000000000010'::uuid,
   'f3000000-0000-0000-0000-000000000006'::uuid,
   'Bug identificado e corrigido: o componente do gráfico somava apenas vendas com client_id não-nulo. Fix aplicado no front. Por favor, recarregue a página e confirme se o problema foi resolvido.',
   'AGENT_MESSAGE','2026-03-05 16:00:00'),
  ('f4000000-0000-0000-0000-000000000011'::uuid,
   'f3000000-0000-0000-0000-000000000006'::uuid,
   'Perfeito! Funcionou. Obrigado!',
   'CUSTOMER_MESSAGE','2026-03-06 08:30:00'),

  -- Ticket 8 (CLOSED · Performance)
  ('f4000000-0000-0000-0000-000000000015'::uuid,
   'f3000000-0000-0000-0000-000000000007'::uuid,
   'Seria muito útil poder exportar os relatórios de fechamento de caixa em PDF para arquivamento mensal.',
   'CUSTOMER_MESSAGE','2026-03-03 11:00:00'),
  ('f4000000-0000-0000-0000-000000000016'::uuid,
   'f3000000-0000-0000-0000-000000000007'::uuid,
   'Registramos sua sugestão no backlog do produto como item de média prioridade. Se quiser, posso encerrar este chamado por enquanto.',
   'AGENT_MESSAGE','2026-03-04 15:00:00'),
  ('f4000000-0000-0000-0000-000000000017'::uuid,
   'f3000000-0000-0000-0000-000000000009'::uuid,
   'O atalho Ctrl+Shift+F continua disponível depois da atualização ou o caminho correto mudou?',
   'CUSTOMER_MESSAGE','2026-03-10 09:00:00'),
  ('f4000000-0000-0000-0000-000000000018'::uuid,
   'f3000000-0000-0000-0000-000000000009'::uuid,
   'Sim, ele continua disponível na versão atual. Se preferir, também pode acessar Operação → Fechamento direto pela barra superior.',
   'AGENT_MESSAGE','2026-03-10 09:40:00'),
  ('f4000000-0000-0000-0000-000000000019'::uuid,
   'f3000000-0000-0000-0000-000000000009'::uuid,
   'Perfeito. Consegui localizar aqui e queria só confirmar porque o operador novo ficou em dúvida.',
   'CUSTOMER_MESSAGE','2026-03-10 10:20:00'),
  ('f4000000-0000-0000-0000-000000000020'::uuid,
   'f3000000-0000-0000-0000-000000000010'::uuid,
   'Gostaria de sugerir um atalho direto para reimprimir o comprovante de uma venda sem entrar no menu.',
   'CUSTOMER_MESSAGE','2026-03-10 11:00:00'),
  ('f4000000-0000-0000-0000-000000000021'::uuid,
   'f3000000-0000-0000-0000-000000000010'::uuid,
   'Obrigado pela sugestão. Já registramos a melhoria para avaliação da equipe de produto. Se estiver tudo certo, me responda e então poderemos encerrar o chamado.',
   'AGENT_MESSAGE','2026-03-10 12:15:00'),

  ('f4000000-0000-0000-0000-000000000012'::uuid,
   'f3000000-0000-0000-0000-000000000008'::uuid,
   'Sistema travando no horário de almoço, todo dia entre 12h e 14h.',
   'CUSTOMER_MESSAGE','2026-02-20 09:00:00'),
  ('f4000000-0000-0000-0000-000000000013'::uuid,
   'f3000000-0000-0000-0000-000000000008'::uuid,
   'Analisamos os logs do servidor: o problema era um índice faltante na tabela de vendas. Adicionamos o índice idx_sale_session_token e o tempo de consulta caiu de 4,8s para 0,12s. Encerrando o chamado.',
   'AGENT_MESSAGE','2026-02-28 15:00:00'),
  ('f4000000-0000-0000-0000-000000000014'::uuid,
   'f3000000-0000-0000-0000-000000000008'::uuid,
   'Ticket encerrado após 7 dias sem recorrência do problema.',
   'INTERNAL_NOTE','2026-03-01 10:00:00')
ON CONFLICT (id) DO NOTHING;

-- ===============================================================
-- BLOCO IV — METAS DE FATURAMENTO
-- ---------------------------------------------------------------
-- Resumo de vendas COMPLETED por período (dados das sessões acima):
--   Semana 03-09/mar/2026: R$ 739,30  (b002, b004–b011)
--   Mês mar/2026 histórico: R$ 739,30 (mesmas sessões, exceto NOW())
--   Fev/2026: R$ 0,00                 (nenhuma sessão no período)
--
-- Metas demonstrativas:
--   (1) CLOSED semanal 03-09/mar  alvo 700,00 → realizado 739,30 → META BATIDA 🎉
--   (2) ACTIVE  mensal  mar/2026  alvo 800,00 → realizado 739,30 → quase lá
--   (3) DRAFT   mensal  abr/2026  alvo 900,00 → sem apuração
--   (4) CLOSED  mensal  fev/2026  alvo 300,00 → realizado   0,00 → não batida
-- ---------------------------------------------------------------

-- 28. METAS DE FATURAMENTO
INSERT INTO goals (id, version, target_value, periodicity, start_date, end_date, status, company_id)
VALUES
  ('a0000000-0000-0000-0000-000000000001'::uuid, 0, 700.00, 'WEEKLY',  '2026-03-03', '2026-03-09', 'CLOSED', 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'),
  ('a0000000-0000-0000-0000-000000000002'::uuid, 0, 800.00, 'MONTHLY', '2026-03-01', '2026-03-31', 'ACTIVE', 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'),
  ('a0000000-0000-0000-0000-000000000003'::uuid, 0, 900.00, 'MONTHLY', '2026-04-01', '2026-04-30', 'DRAFT', 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f'),
  ('a0000000-0000-0000-0000-000000000004'::uuid, 0, 300.00, 'MONTHLY', '2026-02-01', '2026-02-28', 'CLOSED', 'e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f')
ON CONFLICT (id) DO NOTHING;



