-- =============================================================
-- Kalles PDV — Seed de dados para testes
-- Migração repetível Flyway (R__): executa após todas as migrações
-- versionadas (V1–V18) e re-executa quando o conteúdo do arquivo muda.
-- Todos os INSERTs usam ON CONFLICT ... DO NOTHING, portanto é idempotente.
-- =============================================================

-- ---------------------------------------------------------------
-- 1. OPERADORES
--    BASIC      → pode adicionar itens
--    SUPERVISOR → pode remover itens / autorizar BASIC
--    MANAGER    → pode cancelar vendas / autorizar todos
--    ADMIN      → acesso total ao módulo administrativo
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
INSERT INTO cash_registers (code, description, active) VALUES
  ('CAIXA-01', 'Caixa principal — frente de loja',  true),
  ('CAIXA-02', 'Caixa secundário — balcão',         true),
  ('CAIXA-03', 'Caixa self-checkout — Setor A',     true)
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------
-- 3. PRODUTOS
-- ---------------------------------------------------------------
INSERT INTO product (name, internal_code, barcode, description, price, active, version) VALUES
  ('Coca-Cola 350ml',        'PRD-001', '7891000100103', 'Refrigerante lata 350ml',                            5.50,  true,  0),
  ('Água Mineral 500ml',     'PRD-002', '7891000200204', 'Água sem gás 500ml',                                 2.00,  true,  0),
  ('Salgadinho 50g',         'PRD-003', '7891000300305', 'Salgadinho sabor queijo 50g',                        3.75,  true,  0),
  ('Chocolate 100g',         'PRD-004', '7891000400406', 'Barra de chocolate ao leite 100g',                   6.90,  true,  0),
  ('Biscoito Cream',         'PRD-005', '7891000500507', 'Biscoito cream cracker 200g',                        4.50,  true,  0),
  ('Suco de Laranja',        'PRD-006', '7891000600608', 'Néctar de laranja 1L',                               7.00,  true,  0),
  ('Arroz 5kg',              'PRD-007', '7891000700709', 'Arroz branco tipo 1, 5kg',                          22.90,  true,  0),
  ('Feijão 1kg',             'PRD-008', '7891000800800', 'Feijão carioca 1kg',                                10.50,  true,  0),
  ('Óleo de Soja 900ml',     'PRD-009', '7891000900901', 'Óleo de soja refinado 900ml',                        7.80,  true,  0),
  ('Macarrão 500g',          'PRD-010', '7891001001002', 'Macarrão espaguete 500g',                            4.20,  true,  0),
  ('Leite Integral 1L',      'PRD-011', '7891000110011', 'Leite integral tipo A 1L',                           4.50,  true,  0),
  ('Manteiga 200g',          'PRD-012', '7891000120012', 'Manteiga extra creme sem sal 200g',                  8.90,  true,  0),
  ('Iogurte Natural 170g',   'PRD-013', '7891000130013', 'Iogurte natural integral 170g',                      3.20,  true,  0),
  ('Pão de Forma 500g',      'PRD-014', '7891000140014', 'Pão de forma tradicional 500g',                      7.50,  true,  0),
  ('Café Solúvel 50g',       'PRD-015', '7891000150015', 'Café solúvel instantâneo 50g',                       6.80,  true,  0),
  ('Açúcar Refinado 1kg',    'PRD-016', '7891000160016', 'Açúcar refinado branco 1kg',                         4.00,  true,  0),
  ('Sabonete 90g',           'PRD-017', '7891000170017', 'Sabonete hidratante original 90g',                   3.50,  true,  0),
  ('Shampoo 200ml',          'PRD-018', '7891000180018', 'Shampoo restauração 200ml',                         12.90,  true,  0),
  ('Detergente 500ml',       'PRD-019', '7891000190019', 'Detergente neutro 500ml',                            2.50,  true,  0),
  ('Papel Higiênico 4un',    'PRD-020', '7891000200020', 'Papel higiênico dupla folha 4 rolos',               10.90,  true,  0),
  ('Amaciante 2L',           'PRD-021', '7891000210021', 'Amaciante concentrado 2L',                          15.90,  true,  0),
  ('Desinfetante 1L',        'PRD-022', '7891000220022', 'Desinfetante pinho 1L',                              5.40,  true,  0),
  ('Refrigerante Cola 2L',   'PRD-023', '7891000230023', 'Refrigerante cola 2L',                               8.50,  true,  0),
  ('Suco de Uva 1L',         'PRD-024', '7891000240024', 'Suco de uva integral 1L',                           11.00,  true,  0),
  ('Cereal Matinal 300g',    'PRD-025', '7891000250025', 'Cereal matinal de milho 300g',                       9.90,  true,  0),
  ('Fermento em Pó 100g',    'PRD-026', '7891000260026', 'Fermento químico em pó 100g',                        2.80,  true,  0),
  ('Vinagre 750ml',          'PRD-027', '7891000270027', 'Vinagre de álcool 750ml',                            3.10,  true,  0),
  ('Creme de Leite 300g',    'PRD-028', '7891000280028', 'Creme de leite mesa 300g',                           5.20,  true,  0),
  ('Biscoito Recheado 130g', 'PRD-029', '7891000290029', 'Biscoito recheado chocolate — linha descontinuada',  3.90,  false, 0),
  ('Achocolatado 200ml',     'PRD-030', '7891000300030', 'Achocolatado UHT 200ml',                             2.90,  true,  0)
ON CONFLICT (internal_code) DO NOTHING;

-- ---------------------------------------------------------------
-- 4. DEPÓSITOS
-- ---------------------------------------------------------------
INSERT INTO warehouse (name, address, active) VALUES
  ('Depósito Central',  'Rua das Flores, 100 — Bairro Industrial', true),
  ('Depósito Auxiliar', 'Av. Brasil, 500 — Galpão 3',             true)
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
INSERT INTO fidelity_policy (id, objective_points, configured_discount, value_point, active, created_at)
VALUES
  ('a1b2c3d4-0001-0001-0001-000000000001'::uuid,  50, 10.00, 1, false, '2025-09-01'),
  ('a1b2c3d4-0002-0002-0002-000000000002'::uuid, 100, 20.00, 1, true,  '2026-01-01')
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
                  fidelity_discount_applied, points_earned)
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
  0
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
                  fidelity_discount_applied, points_earned)
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
  0
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
                  fidelity_discount_applied, points_earned)
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
  0
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
                  fidelity_discount_applied, points_earned)
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
  0
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
