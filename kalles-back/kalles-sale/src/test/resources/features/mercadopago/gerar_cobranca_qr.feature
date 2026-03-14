# language: pt

Funcionalidade: ACL - Adapter gera cobrança via QR Code Dinâmico no Mercado Pago
  Como sistema ERP Kalles
  Quero que o MercadoPagoOrderAdapter traduza uma intenção de cobrança do domínio
  Para criar uma Order no Mercado Pago via SDK no modo estritamente dinâmico
  Para que a string EMVCo (qr_data) seja retornada de forma limpa ao domínio
  Garantindo idempotência através da X-Idempotency-Key enviada pelo ERP

  Contexto:
    Dado uma Company "COMP-001" que já possui store_id "1234567" registrado no MP
    E um Caixa com external_id "CAIXA-ERP-001" que já possui pos_id "2711382" registrado no MP

  #===========================================================================
  # CENÁRIO 1: GERAÇÃO DE QR DINÂMICO COM SUCESSO
  #===========================================================================

  Cenário: Adapter cria Order dinâmica e retorna qr_data limpo ao domínio
    Dado uma intenção de cobrança com pedidoId "PEDIDO-ERP-9999", valor "50.00", caixa "CAIXA-ERP-001" e idempotencyKey "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    E que o SDK retornará order_id "ORD01K372G4J4FXZ9HGHZMJMGGPKE" e qr_data "00020101021226580014br.gov.bcb.qr0136..."
    Quando o adapter solicitar a criação da Order no Mercado Pago
    Então o SDK deve ter sido invocado com type "qr"
    E o SDK deve ter sido invocado com total_amount "50.00"
    E o SDK deve ter sido invocado com external_reference "PEDIDO-ERP-9999"
    E o SDK deve ter sido invocado com external_pos_id "CAIXA-ERP-001"
    E o SDK deve ter sido invocado com mode "dynamic"
    E o SDK deve ter sido invocado com payment amount "50.00"
    E a X-Idempotency-Key "a1b2c3d4-e5f6-7890-abcd-ef1234567890" deve ter sido passada nas opções do SDK
    E o resultado retornado deve conter o orderId "ORD01K372G4J4FXZ9HGHZMJMGGPKE"
    E o resultado retornado deve conter o qrData "00020101021226580014br.gov.bcb.qr0136..."

  #===========================================================================
  # CENÁRIO 2: MODO SEMPRE DYNAMIC — INVARIANTE DE NEGÓCIO
  #===========================================================================

  Cenário: O campo mode enviado ao SDK é sempre "dynamic" independente de outras configurações
    Dado uma intenção de cobrança com pedidoId "PEDIDO-ERP-1111", valor "120.00", caixa "CAIXA-ERP-001" e idempotencyKey "b2c3d4e5-f6a7-8901-bcde-f12345678901"
    E que o SDK retornará order_id "ORD02..." e qr_data "EMVCo-data-qualquer"
    Quando o adapter solicitar a criação da Order no Mercado Pago
    Então o SDK deve ter sido invocado com mode "dynamic"

  #===========================================================================
  # CENÁRIO 3: PRÉ-CONDIÇÃO — CAIXA SEM POS_ID
  #===========================================================================

  Cenário: Adapter lança exceção se o Caixa não possui pos_id antes de gerar o QR
    Dado uma intenção de cobrança com pedidoId "PEDIDO-ERP-4444", valor "40.00", caixa "CAIXA-SEM-MP" e idempotencyKey "e5f6a7b8-c9d0-1234-ef01-345678901234"
    E que o Caixa "CAIXA-SEM-MP" não possui pos_id registrado no MP
    Quando o adapter solicitar a criação da Order no Mercado Pago
    Então o adapter deve lançar uma IllegalStateException indicando que o POS não foi configurado
    E o SDK NÃO deve ter sido invocado para criação da Order

  #===========================================================================
  # CENÁRIO 4: FALHA — SDK NÃO RETORNA qr_data
  #===========================================================================

  Cenário: Adapter lança exceção quando o SDK não retorna qr_data na resposta
    Dado uma intenção de cobrança com pedidoId "PEDIDO-ERP-2222", valor "30.00", caixa "CAIXA-ERP-001" e idempotencyKey "c3d4e5f6-a7b8-9012-cdef-123456789012"
    E que o SDK retornará uma resposta sem o campo qr_data
    Quando o adapter solicitar a criação da Order no Mercado Pago
    Então o adapter deve lançar uma MercadoPagoIntegrationException indicando ausência do qr_data

  #===========================================================================
  # CENÁRIO 5: FALHA DE COMUNICAÇÃO COM O SDK
  #===========================================================================

  Cenário: Adapter converte exceção do SDK em MercadoPagoIntegrationException ao criar Order
    Dado uma intenção de cobrança com pedidoId "PEDIDO-ERP-3333", valor "75.50", caixa "CAIXA-ERP-001" e idempotencyKey "d4e5f6a7-b8c9-0123-def0-234567890123"
    E que o SDK do Mercado Pago lançará uma exceção de comunicação
    Quando o adapter solicitar a criação da Order no Mercado Pago
    Então o adapter deve lançar uma MercadoPagoIntegrationException
