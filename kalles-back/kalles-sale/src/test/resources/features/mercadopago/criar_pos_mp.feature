# language: pt

Funcionalidade: ACL - Adapter traduz Caixa ERP → POS no Mercado Pago
  Como sistema ERP Kalles (multi-tenant)
  Quero que o MercadoPagoPosAdapter traduza um Caixa do ERP
  Para criar um POS correspondente no Mercado Pago via SDK oficial
  Garantindo que o POS seja vinculado à Store correta com fixed_amount = false

  #===========================================================================
  # CENÁRIO 1: TRADUÇÃO E CRIAÇÃO COM SUCESSO
  #===========================================================================

  Cenário: Adapter traduz corretamente os campos do Caixa e persiste o pos_id retornado
    Dado uma Company "COMP-001" que possui store_id "1234567" gerado no MP
    E um Caixa com id "CAIXA-ERP-001", nome "Caixa 01", pertencente à Company "COMP-001"
    E que o SDK do Mercado Pago retornará o pos_id "2711382" para essa requisição
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o SDK deve ter sido invocado com name "Caixa 01"
    E o SDK deve ter sido invocado com external_id do caixa "CAIXA-ERP-001"
    E o SDK deve ter sido invocado com store_id numérico "1234567"
    E o SDK deve ter sido invocado com external_store_id igual a "COMP-001"
    E o SDK deve ter sido invocado com fixed_amount igual a "false"
    E o pos_id "2711382" deve ter sido persistido vinculado ao Caixa "CAIXA-ERP-001"
    E o resultado retornado deve conter o pos_id "2711382"

  #===========================================================================
  # CENÁRIO 2: IDEMPOTÊNCIA — NÃO RECRIA POS EXISTENTE
  #===========================================================================

  Cenário: Adapter não invoca o SDK se o Caixa já possui pos_id cadastrado
    Dado um Caixa com id "CAIXA-ERP-001" que já possui pos_id "2711382" registrado
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o SDK NÃO deve ter sido invocado para criação do POS
    E o pos_id "2711382" deve ser retornado diretamente ao domínio

  #===========================================================================
  # CENÁRIO 3: PRÉ-CONDIÇÃO — STORE NÃO CONFIGURADA
  #===========================================================================

  Cenário: Adapter lança exceção se a Company não possui store_id antes de criar o POS
    Dado uma Company "COMP-SEM-STORE" que AINDA NÃO possui store_id no MP
    E um Caixa com id "CAIXA-ERP-999", nome "Caixa Órfão", pertencente à Company "COMP-SEM-STORE"
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o adapter deve lançar uma IllegalStateException indicando que a Company não tem Store
    E o SDK NÃO deve ter sido invocado para criação do POS

  #===========================================================================
  # CENÁRIO 4: FALHA DE COMUNICAÇÃO COM O SDK
  #===========================================================================

  Cenário: Adapter converte exceção do SDK em MercadoPagoIntegrationException ao criar POS
    Dado uma Company "COMP-001" que possui store_id "1234567" gerado no MP
    E um Caixa com id "CAIXA-ERP-001", nome "Caixa 01", pertencente à Company "COMP-001"
    E que o SDK lançará uma exceção de comunicação ao criar POS
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o adapter POS deve lançar uma MercadoPagoIntegrationException quando falhar
    E nenhum pos_id deve ter sido persistido para o Caixa "CAIXA-ERP-001"
