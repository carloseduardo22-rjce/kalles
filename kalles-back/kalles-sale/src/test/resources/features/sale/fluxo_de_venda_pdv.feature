# language: pt
Funcionalidade: Fluxo principal de venda no PDV
  Como operador do caixa
  Quero registrar os itens e pagamentos de uma venda
  Para concluir o atendimento sem violar as regras do caixa

  Cenário: Concluir uma venda em dinheiro apos aplicar desconto
    Dado que existe um produto disponivel para venda no PDV
    E um operador autenticado com sessao de caixa aberta em modo normal
    Quando iniciar uma nova venda no PDV
    E adicionar um item pelo codigo interno "SKU-001" com quantidade 1
    E aplicar um desconto de "5.00" no item da venda
    E registrar um pagamento em dinheiro de "30.00"
    E concluir a venda atual
    Então a operacao da venda deve responder com status HTTP 204

  Cenário: Bloquear pagamento eletronico em sessao somente dinheiro
    Dado que existe um produto disponivel para venda no PDV
    E um operador autenticado com sessao de caixa aberta em modo somente dinheiro
    E iniciar uma nova venda no PDV
    E adicionar um item pelo codigo interno "SKU-001" com quantidade 1
    Quando registrar um pagamento via "PIX" no valor de "30.00"
    Então a operacao da venda deve responder com status HTTP 409
    E a resposta da venda deve informar "Esta sessao foi aberta em modo somente dinheiro. PIX, vouchers e cartoes estao indisponiveis."

  Cenário: Bloquear desconto maior que o valor do item
    Dado que existe um produto disponivel para venda no PDV
    E um operador autenticado com sessao de caixa aberta em modo normal
    E iniciar uma nova venda no PDV
    E adicionar um item pelo codigo interno "SKU-001" com quantidade 1
    Quando aplicar um desconto de "35.00" no item da venda
    Então a operacao da venda deve responder com status HTTP 400
    E a resposta da venda deve informar "O desconto não pode exceder o valor do produto. Valor do item: R$ 30.00"

  Cenário: Exigir autorizacao para cancelar venda com operador sem permissao
    Dado que existe um produto disponivel para venda no PDV
    E um operador autenticado com sessao de caixa aberta em modo normal
    E iniciar uma nova venda no PDV
    E adicionar um item pelo codigo interno "SKU-001" com quantidade 1
    Quando solicitar o cancelamento da venda com operador basico
    Então a operacao da venda deve responder com status HTTP 403
    E a resposta da venda deve informar "Operador não possui permissão para cancelar vendas. Solicite autorização de um supervisor."
    Quando solicitar o cancelamento da venda com operador basico autorizado por supervisor
    Então a operacao da venda deve responder com status HTTP 204
