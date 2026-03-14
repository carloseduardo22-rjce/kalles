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
    Dado uma Company "COMP-001" que já possui store_id "1234567" registrado no MP
    E um Caixa com id "CAIXA-ERP-001", nome "Caixa 01", pertencente à Company "COMP-001"
    E que o SDK do Mercado Pago retornará o pos_id "2711382" para essa requisição
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o SDK deve ter sido invocado com name "Caixa 01"
    E o SDK deve ter sido invocado com external_id "CAIXA-ERP-001"
    E o SDK deve ter sido invocado com store_id numérico "1234567"
    E o SDK deve ter sido invocado com external_store_id "COMP-001"
    E o SDK deve ter sido invocado com fixed_amount "false"
    E o pos_id "2711382" deve ter sido persistido vinculado ao Caixa "CAIXA-ERP-001"
    E o resultado retornado deve conter o pos_id "2711382"

  #===========================================================================
  # CENÁRIO 2: IDEMPOTÊNCIA — NÃO RECRIA POS EXISTENTE
  #===========================================================================

  Cenário: Adapter não invoca o SDK se o Caixa já possui pos_id cadastrado
    Dado um Caixa com id "CAIXA-ERP-001" que já possui pos_id "2711382" registrado
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o SDK NÃO deve ter sido invocado
    E o pos_id "2711382" deve ser retornado diretamente ao domínio

  #===========================================================================
  # CENÁRIO 3: PRÉ-CONDIÇÃO — STORE NÃO CONFIGURADA
  #===========================================================================

  Cenário: Adapter lança exceção se a Company não possui store_id antes de criar o POS
    Dado uma Company "COMP-SEM-STORE" que ainda não possui store_id registrado no MP
    E um Caixa com id "CAIXA-ERP-999", nome "Caixa Órfão", pertencente à Company "COMP-SEM-STORE"
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o adapter deve lançar uma IllegalStateException indicando que a Store não foi configurada
    E o SDK NÃO deve ter sido invocado

  #===========================================================================
  # CENÁRIO 4: FALHA DE COMUNICAÇÃO COM O SDK
  #===========================================================================

  Cenário: Adapter converte exceção do SDK em MercadoPagoIntegrationException ao criar POS
    Dado uma Company "COMP-001" que já possui store_id "1234567" registrado no MP
    E um Caixa com id "CAIXA-ERP-001", nome "Caixa 01", pertencente à Company "COMP-001"
    E que o SDK do Mercado Pago lançará uma exceção de comunicação
    Quando o adapter solicitar a criação do POS no Mercado Pago
    Então o adapter deve lançar uma MercadoPagoIntegrationException
    E nenhum pos_id deve ter sido persistido para o Caixa "CAIXA-ERP-001"
