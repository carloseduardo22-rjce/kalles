# language: pt
Funcionalidade: Historico de vendas multi-tenant
  Como gestor de uma filial
  Quero consultar e exportar o historico detalhado das vendas
  Para auditar atendimentos, conferir pagamentos e analisar os registros em planilha

  Contexto:
    Dado que existem dois tenants cadastrados no Kalles
    E o tenant "Alpha" possui a filial "Matriz Alpha"
    E o tenant "Beta" possui a filial "Matriz Beta"
    E existe um operador autenticado para cada tenant

  Cenario: Listar vendas concluidas e canceladas da filial selecionada em ordem decrescente
    Dado que o operador do tenant "Alpha" esta autenticado
    E a filial ativa da requisicao e "Matriz Alpha"
    E existe uma venda "COMPLETED" de "120.00" na filial "Matriz Alpha" aberta em "2026-04-20T10:00:00"
    E existe uma venda "CANCELED" de "80.00" na filial "Matriz Alpha" aberta em "2026-04-21T09:30:00"
    Quando consultar o historico de vendas de "2026-04-01" ate "2026-04-30"
    Entao a operacao deve responder com status HTTP 200
    E a resposta deve conter 2 vendas
    E a primeira venda deve possuir estado "CANCELED"
    E a segunda venda deve possuir estado "COMPLETED"
    E cada venda deve informar id, sessionToken, companyId, state, subtotal, total, amountDue, fidelityDiscountApplied e pointsEarned
    E cada venda deve informar seus itens e pagamentos vinculados

  Cenario: Filtrar historico por estado da venda
    Dado que o operador do tenant "Alpha" esta autenticado
    E a filial ativa da requisicao e "Matriz Alpha"
    E existe uma venda "COMPLETED" de "50.00" na filial "Matriz Alpha" aberta em "2026-04-10T11:00:00"
    E existe uma venda "CANCELED" de "30.00" na filial "Matriz Alpha" aberta em "2026-04-10T12:00:00"
    Quando consultar o historico de vendas de "2026-04-01" ate "2026-04-30" com estado "COMPLETED"
    Entao a operacao deve responder com status HTTP 200
    E a resposta deve conter 1 venda
    E a venda retornada deve possuir estado "COMPLETED"

  Cenario: Rejeitar consulta com periodo invalido
    Dado que o operador do tenant "Alpha" esta autenticado
    E a filial ativa da requisicao e "Matriz Alpha"
    Quando consultar o historico de vendas de "2026-04-30" ate "2026-04-01"
    Entao a operacao deve responder com status HTTP 400
    E a resposta deve informar "A data final nao pode ser menor que a data inicial."

  Cenario: Exigir contexto de filial para consultar historico
    Dado que o operador do tenant "Alpha" esta autenticado
    Quando consultar o historico de vendas de "2026-04-01" ate "2026-04-30" sem informar filial ativa
    Entao a operacao deve responder com status HTTP 400
    E a resposta deve informar "Esta rota exige uma filial ativa. Envie X-Company-ID com uma filial acessivel para o tenant autenticado."

  Cenario: Isolar vendas por tenant e filial
    Dado que o operador do tenant "Alpha" esta autenticado
    E a filial ativa da requisicao e "Matriz Alpha"
    E existe uma venda "COMPLETED" de "100.00" na filial "Matriz Alpha" aberta em "2026-04-15T08:00:00"
    E existe uma venda "COMPLETED" de "200.00" na filial "Matriz Beta" aberta em "2026-04-15T08:30:00"
    Quando consultar o historico de vendas de "2026-04-01" ate "2026-04-30"
    Entao a operacao deve responder com status HTTP 200
    E a resposta deve conter 1 venda
    E a venda retornada deve pertencer a filial "Matriz Alpha"
    E a resposta nao deve conter vendas do tenant "Beta"

  Cenario: Exportar historico de vendas para Excel com colunas das entidades
    Dado que o operador do tenant "Alpha" esta autenticado
    E a filial ativa da requisicao e "Matriz Alpha"
    E existe uma venda "COMPLETED" de "120.00" na filial "Matriz Alpha" com itens e pagamentos em "2026-04-20T10:00:00"
    Quando exportar o historico de vendas de "2026-04-01" ate "2026-04-30" para Excel
    Entao a operacao deve responder com status HTTP 200
    E o arquivo deve ser retornado com tipo "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    E o arquivo deve possuir a aba "sales"
    E a aba "sales" deve possuir as colunas "id, version, session_token, company_id, state, client_id, subtotal, total, amount_due, fidelity_discount_applied, points_earned"
    E o arquivo deve possuir a aba "sale_items"
    E a aba "sale_items" deve possuir as colunas "id, sale_id, product_id, quantity, unit_price, discount"
    E o arquivo deve possuir a aba "payments"
    E a aba "payments" deve possuir as colunas "id, sale_id, method, amount, change_amount, transaction_id, confirmed, created_at, updated_at"
    E nenhuma aba deve conter vendas, itens ou pagamentos de outro tenant
