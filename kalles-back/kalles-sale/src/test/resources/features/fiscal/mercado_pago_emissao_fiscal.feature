# language: pt
@fiscal @mercadopago @multi_tenant @wip

Funcionalidade: Integracao fiscal com emissor Mercado Pago
  Como administrador do ERP Kalles
  Quero conciliar documentos fiscais emitidos pelo Mercado Pago com as vendas do Kalles
  Para evitar duplicidade fiscal e manter rastreabilidade por tenant e filial

  Cenario: Marcar venda Mercado Pago aprovada como elegivel para NFC-e propria
    Dado que existe uma venda do tenant atual paga pelo Mercado Pago Point
    E a venda ainda nao possui documento fiscal no Kalles
    Quando o pagamento Mercado Pago for confirmado
    Entao a venda deve ficar elegivel para emissao de NFC-e no Kalles
    E a elegibilidade fiscal deve pertencer ao tenant atual
    E a elegibilidade fiscal deve pertencer a filial ativa

  Cenario: Registrar chave de NFC-e emitida externamente pelo Mercado Pago
    Dado que existe uma venda do tenant atual paga pelo Mercado Pago Point
    E o emissor Mercado Pago autorizou uma NFC-e para essa venda
    Quando o usuario registrar a chave de acesso da NFC-e externa
    Entao o documento fiscal externo deve ser vinculado a venda
    E o documento fiscal deve registrar o provider "MERCADO_PAGO"
    E a venda nao deve permitir nova emissao de NFC-e propria

  Cenario: Bloquear emissao propria quando ja existe NFC-e externa Mercado Pago
    Dado que existe uma venda com NFC-e emitida pelo Mercado Pago
    Quando o usuario solicitar a emissao da NFC-e pelo Kalles
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "A venda ja possui documento fiscal emitido pelo Mercado Pago"

  Cenario: Registrar nota de devolucao a partir de reembolso Mercado Pago
    Dado que existe uma venda com pagamento Mercado Pago aprovado
    E existe uma NFC-e autorizada para essa venda
    Quando o Mercado Pago confirmar o reembolso do pagamento
    Entao a venda deve ficar elegivel para nota fiscal de devolucao
    E a elegibilidade de devolucao deve preservar tenant e filial da venda original
