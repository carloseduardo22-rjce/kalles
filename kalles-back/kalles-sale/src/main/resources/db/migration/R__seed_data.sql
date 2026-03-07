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
