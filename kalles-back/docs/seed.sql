-- =============================================================
-- Kalles PDV — Seed de dados para testes
-- Execute no banco "kalles" após subir o backend (Flyway cria as tabelas)
-- =============================================================

-- ---------------------------------------------------------------
-- 1. OPERADORES
--    BASIC  → pode adicionar itens
--    SUPERVISOR → pode remover itens / autorizar BASIC
--    MANAGER    → pode cancelar vendas / autorizar todos
-- ---------------------------------------------------------------
INSERT INTO operators (name, code, permission_level) VALUES
  ('João Silva',   'OP-001', 'BASIC'),
  ('Maria Santos', 'OP-002', 'SUPERVISOR'),
  ('Pedro Costa',  'OP-003', 'MANAGER')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. CAIXAS
-- ---------------------------------------------------------------
INSERT INTO cash_registers (code, description, active) VALUES
  ('CAIXA-01', 'Caixa principal — frente de loja', true),
  ('CAIXA-02', 'Caixa secundário — balcão',        true)
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------
-- 3. PRODUTOS
-- ---------------------------------------------------------------
INSERT INTO product (name, internal_code, barcode, description, price, active, version) VALUES
  ('Coca-Cola 350ml',   'PRD-001', '7891000100103', 'Refrigerante lata 350ml',           5.50,  true, 0),
  ('Água Mineral 500ml','PRD-002', '7891000200204', 'Água sem gás 500ml',                2.00,  true, 0),
  ('Salgadinho 50g',    'PRD-003', '7891000300305', 'Salgadinho sabor queijo 50g',       3.75,  true, 0),
  ('Chocolate 100g',    'PRD-004', '7891000400406', 'Barra de chocolate ao leite 100g',  6.90,  true, 0),
  ('Biscoito Cream',    'PRD-005', '7891000500507', 'Biscoito cream cracker 200g',       4.50,  true, 0),
  ('Suco de Laranja',   'PRD-006', '7891000600608', 'Néctar de laranja 1L',              7.00,  true, 0),
  ('Arroz 5kg',         'PRD-007', '7891000700709', 'Arroz branco tipo 1, 5kg',         22.90,  true, 0),
  ('Feijão 1kg',        'PRD-008', '7891000800800', 'Feijão carioca 1kg',               10.50,  true, 0),
  ('Óleo de Soja 900ml','PRD-009', '7891000900901', 'Óleo de soja refinado 900ml',       7.80,  true, 0),
  ('Macarrão 500g',     'PRD-010', '7891001001002', 'Macarrão espaguete 500g',           4.20,  true, 0)
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
-- 6. ESTOQUE  (distribui os produtos entre as localizações)
-- ---------------------------------------------------------------
INSERT INTO stock (product_id, location_id, quantity, version)
SELECT p.id, l.id, qty.quantity, 0
FROM (
  VALUES
    ('PRD-001', 'Depósito Central',  'A1', 80),
    ('PRD-001', 'Depósito Auxiliar', 'X1', 20),
    ('PRD-002', 'Depósito Central',  'A1', 60),
    ('PRD-002', 'Depósito Auxiliar', 'X1', 40),
    ('PRD-003', 'Depósito Central',  'A2', 100),
    ('PRD-004', 'Depósito Central',  'A2', 50),
    ('PRD-004', 'Depósito Auxiliar', 'X2', 50),
    ('PRD-005', 'Depósito Central',  'B1', 100),
    ('PRD-006', 'Depósito Central',  'B1', 70),
    ('PRD-006', 'Depósito Auxiliar', 'X2', 30),
    ('PRD-007', 'Depósito Central',  'A1', 90),
    ('PRD-008', 'Depósito Central',  'A2', 85),
    ('PRD-009', 'Depósito Central',  'B1', 60),
    ('PRD-010', 'Depósito Auxiliar', 'X1', 100)
) AS qty(internal_code, warehouse_name, location_code, quantity)
JOIN product  p ON p.internal_code   = qty.internal_code
JOIN warehouse w ON w.name            = qty.warehouse_name
JOIN location  l ON l.warehouse_id    = w.id AND l.code = qty.location_code
ON CONFLICT (product_id, location_id) DO NOTHING;

-- ---------------------------------------------------------------
-- 7. CLIENTES
-- ---------------------------------------------------------------
INSERT INTO client (name, birth_date, gender, cpf, code_country, cellphone, rg, name_father, name_mother, observations) VALUES
  ('Ana Paula Souza',    '1992-03-15', 'F', '529.982.247-25', '+55', '11991110001', '12345678',  'Carlos Souza',  'Marta Souza',  NULL),
  ('Bruno Ferreira',     '1985-07-22', 'M', '153.509.460-56', '+55', '21992220002', '23456789',  'Luiz Ferreira', 'Rosa Ferreira', 'Cliente VIP'),
  ('Carla Mendes',       '1998-11-05', 'F', '046.270.070-54', '+55', '31993330003', '34567890',  NULL,            'Sônia Mendes', NULL),
  ('Diego Lima',         '1990-01-30', 'M', '168.995.350-09', '+55', '41994440004', '45678901',  'Paulo Lima',    'Clara Lima',   NULL),
  ('Fernanda Castro',    '2000-08-18', 'F', '942.836.530-06', '+55', '51995550005', NULL,        NULL,            NULL,           NULL)
ON CONFLICT (cpf) DO NOTHING;
