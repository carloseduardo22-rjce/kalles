# language: pt
Funcionalidade: Selecao global de loja e visibilidade de integracoes disponiveis
  Como usuario autenticado de um tenant com mais de uma loja
  Quero escolher a loja ativa em um local global da aplicacao
  Para manipular produtos, caixas, vendas e relatorios sempre no contexto correto

  Contexto:
    Dado que existe um tenant "tenant-a" com as lojas "Matriz" e "Filial Centro"
    E que estou autenticado no tenant "tenant-a"

  Cenario: Selecionar loja ativa fora da tela de lojas
    Quando eu acessar uma tela operacional da aplicacao
    Entao devo ver o seletor "Loja ativa"
    Quando eu selecionar a loja "Filial Centro"
    Entao as proximas chamadas protegidas devem usar o contexto da loja "Filial Centro"

  Cenario: Isolar dados ao alternar a loja ativa
    Dado que a loja "Matriz" possui o caixa "CX-MATRIZ"
    E que a loja "Filial Centro" possui o caixa "CX-CENTRO"
    Quando eu selecionar a loja "Filial Centro"
    E eu listar os caixas
    Entao a lista deve conter "CX-CENTRO"
    E a lista nao deve conter "CX-MATRIZ"

  Cenario: Exibir apenas integracao Mercado Pago nas telas visuais
    Quando eu acessar a configuracao de pagamentos
    Entao devo ver a opcao "Mercado Pago"
    E nao devo ver a opcao "Stone"

  Cenario: Manter operacao de cartao local enquanto Stone esta sem onboarding visual
    Dado que existe uma venda aberta no PDV
    Quando eu selecionar pagamento com cartao
    Entao a tela deve permitir pagar sem exibir fluxo visual da Stone
