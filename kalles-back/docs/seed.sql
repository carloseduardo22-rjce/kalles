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
-- 3. PRODUTOS  (com estoque inicial = 100 unidades cada)
-- ---------------------------------------------------------------
INSERT INTO product (name, internal_code, barcode, description, price, active, stock_quantity, version) VALUES
  ('Coca-Cola 350ml',   'PRD-001', '7891000100103', 'Refrigerante lata 350ml',           5.50,  true, 100, 0),
  ('Água Mineral 500ml','PRD-002', '7891000200204', 'Água sem gás 500ml',                2.00,  true, 100, 0),
  ('Salgadinho 50g',    'PRD-003', '7891000300305', 'Salgadinho sabor queijo 50g',       3.75,  true, 100, 0),
  ('Chocolate 100g',    'PRD-004', '7891000400406', 'Barra de chocolate ao leite 100g',  6.90,  true, 100, 0),
  ('Biscoito Cream',    'PRD-005', '7891000500507', 'Biscoito cream cracker 200g',       4.50,  true, 100, 0),
  ('Suco de Laranja',   'PRD-006', '7891000600608', 'Néctar de laranja 1L',              7.00,  true, 100, 0),
  ('Arroz 5kg',         'PRD-007', '7891000700709', 'Arroz branco tipo 1, 5kg',         22.90,  true, 100, 0),
  ('Feijão 1kg',        'PRD-008', '7891000800800', 'Feijão carioca 1kg',               10.50,  true, 100, 0),
  ('Óleo de Soja 900ml','PRD-009', '7891000900901', 'Óleo de soja refinado 900ml',       7.80,  true, 100, 0),
  ('Macarrão 500g',     'PRD-010', '7891001001002', 'Macarrão espaguete 500g',           4.20,  true, 100, 0)
ON CONFLICT (internal_code) DO NOTHING;
