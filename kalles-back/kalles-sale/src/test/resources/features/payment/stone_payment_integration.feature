# language: pt
@payment @stone
Funcionalidade: Integracao da Stone Connect 2.0 no contexto generico de payment
  Como o ERP Kalles
  Quero expor as capacidades da Stone Connect 2.0 pelo contexto generico de payment
  Para operar terminais Stone sem acoplamento do frontend e do core ao contrato externo do provider

  Contexto:
    Dado que o provider de pagamento "STONE" esta habilitado no contexto de payment

  Cenario: Criar pedido Stone em fluxo de pagamento direto com cartao de credito
    Dado uma solicitacao generica de pagamento com provider "STONE"
    E o flow informado e "TERMINAL"
    E o externalReference informado e "ERP-SALE-1001"
    E o amount informado e "125.00"
    E o targetId informado e "6N021234"
    E o methodType informado e "CREDIT_CARD"
    E a metadata contem:
      | key                | value            |
      | stoneFlow          | DIRECT           |
      | customerName       | Tony Stark       |
      | customerEmail      | tony@kalles.com  |
      | itemDescription    | Venda PDV 1001   |
      | itemCode           | SKU-1001         |
      | deviceSerialNumber | 6N021234         |
      | displayName        | Pedido #1001     |
      | printOrderReceipt  | false            |
      | visible            | true             |
      | installmentCount   | 1                |
      | installmentType    | merchant         |
    Quando o cliente processar o pagamento no endpoint generico de pagamentos
    Entao a resposta deve ter status HTTP 200
    E a resposta deve conter o provider "STONE"
    E a resposta deve conter um providerOrderId preenchido
    E a resposta deve conter status "PENDING"

  Cenario: Criar pedido Stone em fluxo de pagamento direto com Pix
    Dado uma solicitacao generica de pagamento com provider "STONE"
    E o flow informado e "TERMINAL"
    E o externalReference informado e "ERP-SALE-1002"
    E o amount informado e "50.00"
    E o targetId informado e "6N021235"
    E a metadata contem:
      | key                | value             |
      | stoneFlow          | DIRECT            |
      | paymentType        | pix               |
      | customerName       | Pepper Potts      |
      | customerEmail      | pepper@kalles.com |
      | itemDescription    | Venda PIX 1002    |
      | itemCode           | SKU-1002          |
      | deviceSerialNumber | 6N021235          |
      | displayName        | Pedido #1002      |
      | printOrderReceipt  | true              |
      | visible            | true              |
      | installmentCount   | 1                 |
      | installmentType    | merchant          |
    Quando o cliente processar o pagamento no endpoint generico de pagamentos
    Entao a resposta deve ter status HTTP 200
    E a resposta deve conter o provider "STONE"
    E a resposta deve conter um providerOrderId preenchido
    E a resposta deve conter status "PENDING"

  Cenario: Criar pedido Stone em fluxo de listagem de pedidos
    Dado uma solicitacao generica de pagamento com provider "STONE"
    E o flow informado e "TERMINAL"
    E o externalReference informado e "ERP-SALE-1003"
    E o amount informado e "89.90"
    E o targetId informado e "6N021236"
    E a metadata contem:
      | key                | value            |
      | stoneFlow          | LIST             |
      | customerName       | Bruce Banner     |
      | customerEmail      | bruce@kalles.com |
      | itemDescription    | Venda PDV 1003   |
      | itemCode           | SKU-1003         |
      | deviceSerialNumber | 6N021236         |
      | displayName        | Mesa 7           |
      | printOrderReceipt  | false            |
      | visible            | true             |
    Quando o cliente processar o pagamento no endpoint generico de pagamentos
    Entao a resposta deve ter status HTTP 200
    E a resposta deve conter o provider "STONE"
    E a resposta deve conter um providerOrderId preenchido
    E a resposta deve conter status "PENDING"

  Cenario: Consultar pedido Stone por resiliencia
    Dado que existe um pagamento Stone previamente criado com providerOrderId "or_stone_123"
    Quando o cliente consultar o endpoint generico de pagamentos com provider "STONE" e providerOrderId "or_stone_123"
    Entao a resposta deve ter status HTTP 200
    E a resposta deve conter o provider "STONE"
    E a resposta deve conter o providerOrderId "or_stone_123"
    E a resposta deve conter um status de pagamento valido do dominio

  Cenario: Fechar pedido Stone como pago apos charge.paid
    Dado que existe um pedido Stone aberto com providerOrderId "or_stone_paid_1"
    E o pagamento do pedido foi confirmado por webhook
    Quando o sistema solicitar o fechamento do pedido Stone com status final "paid"
    Entao a resposta de fechamento deve ter status HTTP 200
    E o pedido Stone deve ficar invisivel para o POS

  Cenario: Cancelar pedido Stone pendente antes do pagamento
    Dado que existe um pedido Stone aberto com providerOrderId "or_stone_pending_1"
    Quando o cliente solicitar o cancelamento generico do pagamento com provider "STONE" e providerOrderId "or_stone_pending_1"
    Entao a resposta de cancelamento deve ter status HTTP 200
    E o pedido Stone deve ser fechado com status final "canceled"

  Cenario: Imprimir nota fiscal apos pagamento Stone
    Dado que existe um pedido Stone pago com providerOrderId "or_stone_print_1"
    E existe um documento fiscal para impressao com:
      | key     | value                             |
      | type    | NFE                               |
      | size_v  | 128                               |
      | size_h  | 384                               |
      | format  | png                               |
      | content | A1b2cDefghiJkWlMn9PQrStUVABCDEF== |
    Quando o sistema solicitar a impressao do documento no terminal Stone
    Entao a resposta de impressao deve ter status HTTP 200

  Cenario: Retornar conflito ao imprimir novamente um documento Stone ja em processamento
    Dado que existe um pedido Stone pago com providerOrderId "or_stone_print_2"
    E ja existe uma requisicao de impressao em processamento para esse pedido
    Quando o sistema solicitar a impressao do documento no terminal Stone
    Entao a resposta de impressao deve ter status HTTP 409

  Cenario: Confirmar pagamento Stone por webhook charge.paid
    Dado um webhook Stone do tipo "charge.paid"
    E o webhook referencia o providerOrderId "or_stone_123"
    E o webhook referencia o providerPaymentId "ch_stone_987"
    E o webhook informa status externo "paid"
    E o webhook informa paymentMethod externo "credit"
    E o webhook informa terminalSerialNumber "6N021234"
    Quando o provider enviar o callback para o webhook especifico da Stone
    Entao a resposta do webhook deve ter status HTTP 200
    E o evento deve ser traduzido para o provider "STONE"
    E o evento deve ser traduzido para o status de pagamento "APPROVED"
    E o evento deve preservar o providerOrderId "or_stone_123"
    E o evento deve preservar o providerPaymentId "ch_stone_987"

  Cenario: Aceitar charge.paid de valor diferente no fluxo de listagem de pedidos
    Dado um webhook Stone do tipo "charge.paid"
    E o webhook referencia o providerOrderId "or_stone_list_1"
    E o webhook referencia o providerPaymentId "ch_stone_list_1"
    E o webhook informa status externo "paid"
    E o webhook informa paidAmount externo "100.00"
    E o pedido original Stone foi criado com amount "89.90" no fluxo "LIST"
    Quando o provider enviar o callback para o webhook especifico da Stone
    Entao a resposta do webhook deve ter status HTTP 200
    E o evento deve ser traduzido para o provider "STONE"
    E o evento deve ser traduzido para o status de pagamento "APPROVED"
    E o evento deve registrar na metadata que o valor pago divergiu do valor original

  Cenario: Confirmar estorno Stone por webhook charge.refunded
    Dado um webhook Stone do tipo "charge.refunded"
    E o webhook referencia o providerOrderId "or_stone_124"
    E o webhook referencia o providerPaymentId "ch_stone_988"
    E o webhook informa status externo "canceled"
    Quando o provider enviar o callback para o webhook especifico da Stone
    Entao a resposta do webhook deve ter status HTTP 200
    E o evento deve ser traduzido para o provider "STONE"
    E o evento deve ser traduzido para o status de pagamento "REFUNDED"

  Cenario: Aceitar transacao avulsa Stone recebida apenas por webhook
    Dado um webhook Stone do tipo "charge.paid"
    E o webhook referencia o providerOrderId "or_stone_avulso_1"
    E o webhook referencia o providerPaymentId "ch_stone_avulso_1"
    E o pedido do webhook nao existe previamente no ERP
    Quando o provider enviar o callback para o webhook especifico da Stone
    Entao a resposta do webhook deve ter status HTTP 200
    E o evento deve ser traduzido para o provider "STONE"
    E o evento deve ser traduzido para o status de pagamento "APPROVED"
    E o sistema deve aceitar o evento sem exigir pedido previamente persistido

  Cenario: Recusar pedido Stone sem terminal informado
    Dado uma solicitacao generica de pagamento com provider "STONE"
    E o flow informado e "TERMINAL"
    E o externalReference informado e "ERP-SALE-1004"
    E o amount informado e "42.00"
    E o targetId informado e ""
    Quando o cliente processar o pagamento no endpoint generico de pagamentos
    Entao a resposta deve ter status HTTP 400
    E a resposta deve conter a mensagem "targetId is required"

  Cenario: Recusar pedido Stone sem identificacao minima do cliente
    Dado uma solicitacao generica de pagamento com provider "STONE"
    E o flow informado e "TERMINAL"
    E o externalReference informado e "ERP-SALE-1005"
    E o amount informado e "42.00"
    E o targetId informado e "6N021240"
    E a metadata contem:
      | key                | value      |
      | stoneFlow          | DIRECT     |
      | itemDescription    | Venda 1005 |
      | deviceSerialNumber | 6N021240   |
    Quando o cliente processar o pagamento no endpoint generico de pagamentos
    Entao a resposta deve ter status HTTP 400
    E a resposta deve conter a mensagem "customerName is required for STONE"

  Cenario: Recusar pedido Stone acima do limite operacional de pedidos abertos
    Dado que ja existem 30 pedidos Stone abertos para o terminal "6N021241"
    E uma solicitacao generica de pagamento com provider "STONE"
    E o flow informado e "TERMINAL"
  E o externalReference informado e "ERP-SALE-1006"
  E o amount informado e "10.00"
  E o targetId informado e "6N021241"
  E a metadata contem:
    | key             | value            |
    | stoneFlow       | LIST             |
    | customerName    | Natasha Romanoff |
    | itemDescription | Venda 1006       |
  Quando o cliente processar o pagamento no endpoint generico de pagamentos
  Entao a resposta deve ter status HTTP 409
  E a resposta deve conter a mensagem "STONE open order limit reached for terminal"

  Cenario: Ignorar webhook Stone com tipo nao suportado
    Dado um webhook Stone do tipo "charge.unknown"
    Quando o provider enviar o callback para o webhook especifico da Stone
    Entao a resposta do webhook deve ter status HTTP 202
    E o sistema nao deve alterar nenhum pagamento
