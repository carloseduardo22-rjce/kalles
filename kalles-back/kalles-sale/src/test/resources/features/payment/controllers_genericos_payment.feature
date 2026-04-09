# language: pt
@payment

Funcionalidade: Controllers genericos de payment
  Como frontend do Kalles
  Quero falar com contratos genericos do bounded context payment
  Para trocar provedores sem reescrever a camada de entrada

  Cenario: Vinculacao de conta usa provider e metadata genericos
    Dado uma solicitacao generica de vinculacao com provider "MERCADO_PAGO", authorizationCode "AUTH-123", state "STATE-001", metadata "callbackUrl" "https://kalles/callback"
    Quando o controller generico de vinculacao processar a solicitacao
    Entao o caso de uso de vinculacao deve receber o provider "MERCADO_PAGO"
    E o caso de uso de vinculacao deve receber o authorizationCode "AUTH-123"
    E o caso de uso de vinculacao deve receber o state "STATE-001"
    E o caso de uso de vinculacao deve receber metadata "callbackUrl" "https://kalles/callback"
    E a resposta de vinculacao deve ter status HTTP 200

  Cenario: Criacao de loja devolve dados do dominio generico
    Dado uma solicitacao generica de loja com provider "MERCADO_PAGO", externalReference "COMP-001" e companyId "11111111-1111-1111-1111-111111111111"
    E o caso de uso generico de loja retornara o providerStoreId "STORE-123"
    Quando o controller generico de loja processar a solicitacao
    Entao o caso de uso de loja deve receber o provider "MERCADO_PAGO"
    E o caso de uso de loja deve receber o externalReference "COMP-001"
    E a resposta de loja deve conter o provider "MERCADO_PAGO"
    E a resposta de loja deve conter o providerStoreId "STORE-123"

  Cenario: Processamento de pagamento preserva o contrato generico
    Dado uma solicitacao generica de pagamento com provider "MERCADO_PAGO", flow "QR_CODE", externalReference "ERP-ORDER-1", amount "50.00", targetId "CAIXA-01", metadata "soft_descriptor" "KALLES"
    E o caso de uso generico de pagamento retornara orderId "MP-ORDER-1", status "CREATED", metadata "qrData" "000201010212"
    Quando o controller generico de pagamentos processar a solicitacao
    Entao o caso de uso de pagamento deve receber o provider "MERCADO_PAGO"
    E o caso de uso de pagamento deve receber o flow "QR_CODE"
    E o caso de uso de pagamento deve receber o targetId "CAIXA-01"
    E o caso de uso de pagamento deve receber metadata "soft_descriptor" "KALLES"
    E a resposta de pagamento deve conter o provider "MERCADO_PAGO"
    E a resposta de pagamento deve conter o orderId "MP-ORDER-1"
    E a resposta de pagamento deve conter o status "CREATED"
    E a resposta de pagamento deve conter metadata "qrData" "000201010212"

  Cenario: Fechamento de pedido usa status generico do dominio
    Dado um pedido generico existente com provider "STONE" e orderId "OR-STONE-1"
    Quando o controller generico de pagamentos solicitar o fechamento com status "APPROVED"
    Entao o caso de uso de fechamento deve receber o provider "STONE"
    E o caso de uso de fechamento deve receber o orderId "OR-STONE-1"
    E o caso de uso de fechamento deve receber o status "APPROVED"
    E a resposta de fechamento generico deve ter status HTTP 200

  Cenario: Impressao de documento usa contrato generico do bounded context payment
    Dado uma solicitacao generica de impressao com provider "STONE", orderId "OR-STONE-2", type "NFE", sizeVertical 128, sizeHorizontal 384, format "png" e content "BASE64"
    Quando o controller generico de pagamentos solicitar a impressao do documento
    Entao o caso de uso de impressao deve receber o provider "STONE"
    E o caso de uso de impressao deve receber o orderId "OR-STONE-2"
    E o caso de uso de impressao deve receber o type "NFE"
    E a resposta de impressao generica deve ter status HTTP 200
