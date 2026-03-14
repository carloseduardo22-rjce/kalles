# language: pt

Funcionalidade: ACL - Adapter traduz Company ERP → Store no Mercado Pago
  Como sistema ERP Kalles (multi-tenant)
  Quero que o MercadoPagoStoreAdapter traduza uma Company do ERP
  Para criar uma Store correspondente no Mercado Pago via SDK oficial
  Para que o store_id retornado seja persistido vinculado à Company

  #===========================================================================
  # CENÁRIO 1: TRADUÇÃO E CRIAÇÃO COM SUCESSO
  #===========================================================================

  Cenário: Adapter traduz corretamente os campos da Company e persiste o store_id retornado
    Dado uma Company com id "COMP-001", nome "Kalles Matriz", logradouro "Rua das Flores", numero "123", cidade "São Paulo", estado "SP", latitude "-23.550520" e longitude "-46.633308"
    E que o SDK do Mercado Pago retornará o store_id "1234567" para essa requisição
    Quando o adapter solicitar a criação da Store no Mercado Pago
    Então o SDK deve ter sido invocado com name "Kalles Matriz"
    E o SDK deve ter sido invocado com external_id "COMP-001"
    E o SDK deve ter sido invocado com street_name "Rua das Flores"
    E o SDK deve ter sido invocado com street_number "123"
    E o SDK deve ter sido invocado com city_name "São Paulo"
    E o SDK deve ter sido invocado com state_name "SP"
    E o SDK deve ter sido invocado com latitude "-23.550520"
    E o SDK deve ter sido invocado com longitude "-46.633308"
    E o store_id "1234567" deve ter sido persistido vinculado à Company "COMP-001"
    E o resultado retornado deve conter o store_id "1234567"

  #===========================================================================
  # CENÁRIO 2: IDEMPOTÊNCIA — NÃO RECRIA STORE EXISTENTE
  #===========================================================================

  Cenário: Adapter não invoca o SDK se a Company já possui store_id cadastrado
    Dado uma Company com id "COMP-001" que já possui store_id "1234567" registrado
    Quando o adapter solicitar a criação da Store no Mercado Pago
    Então o SDK NÃO deve ter sido invocado
    E o store_id "1234567" deve ser retornado diretamente ao domínio

  #===========================================================================
  # CENÁRIO 3: FALHA DE COMUNICAÇÃO COM O SDK
  #===========================================================================

  Cenário: Adapter converte exceção do SDK em MercadoPagoIntegrationException
    Dado uma Company com id "COMP-ERR" e nome "Empresa Inválida" sem store_id cadastrado
    E que o SDK do Mercado Pago lançará uma exceção de comunicação
    Quando o adapter solicitar a criação da Store no Mercado Pago
    Então o adapter deve lançar uma MercadoPagoIntegrationException
    E nenhum store_id deve ter sido persistido para a Company "COMP-ERR"
