# language: pt
@payment @stone @multi_tenant @pdv
Funcionalidade: Mapeamento operacional de terminal Stone por caixa no PDV
  Como administrador e operador do Kalles
  Quero mapear uma maquininha Stone para um caixa e usar esse mapeamento no PDV
  Para criar pedidos Stone Connect 2.0 sem expor detalhes da API externa nas telas

  Contexto:
    Dado que o provider de pagamento "STONE" esta habilitado
    E que existe um tenant "tenant-a" com a filial "loja-a"
    E que existe um tenant "tenant-b" com a filial "loja-b"
    E que existe um caixa "CAIXA-01" na filial "loja-a"
    E que existe um caixa "CAIXA-02" na filial "loja-b"

  Cenario: Admin mapeia uma maquininha Stone para um caixa da filial ativa
    Dado que estou autenticado como admin do tenant "tenant-a"
    E que a filial ativa e "loja-a"
    Quando eu mapear o terminal Stone serial "6N021234" para o caixa "CAIXA-01"
    Entao a resposta de mapeamento Stone deve ter status HTTP 201
    E o mapeamento Stone deve pertencer ao tenant "tenant-a"
    E o mapeamento Stone deve pertencer a filial "loja-a"
    E o mapeamento Stone deve vincular o caixa "CAIXA-01" ao serial "6N021234"

  Cenario: Nao permitir mapear terminal Stone para caixa de outro tenant
    Dado que estou autenticado como admin do tenant "tenant-a"
    E que a filial ativa e "loja-a"
    Quando eu tentar mapear o terminal Stone serial "6N999999" para o caixa "CAIXA-02"
    Entao a resposta de mapeamento Stone deve ter status HTTP 404
    E nenhum mapeamento Stone deve ser criado para o tenant "tenant-b"

  Cenario: Nao permitir dois caixas ativos usando o mesmo serial Stone na mesma filial
    Dado que estou autenticado como admin do tenant "tenant-a"
    E que a filial ativa e "loja-a"
    E que o terminal Stone serial "6N021234" ja esta mapeado para o caixa "CAIXA-01"
    E que existe um caixa "CAIXA-03" na filial "loja-a"
    Quando eu tentar mapear o terminal Stone serial "6N021234" para o caixa "CAIXA-03"
    Entao a resposta de mapeamento Stone deve ter status HTTP 409
    E a resposta deve informar que o terminal Stone ja esta vinculado a outro caixa ativo

  Cenario: PDV escolhe automaticamente o terminal Stone associado ao caixa da sessao
    Dado que estou autenticado como operador do tenant "tenant-a"
    E que a filial ativa e "loja-a"
    E que existe uma sessao aberta no caixa "CAIXA-01" com pagamento eletronico habilitado
    E que o terminal Stone serial "6N021234" esta mapeado para o caixa "CAIXA-01"
    Quando eu iniciar uma venda no PDV
    E eu solicitar pagamento Stone por "CREDIT_CARD" no valor de "125.00"
    Entao a resposta do pedido Stone deve ter status HTTP 200
    E o backend deve criar o pedido Stone com targetId "6N021234"
    E o pedido Stone deve receber metadata "customerName"
    E o pedido Stone deve receber metadata "itemDescription"
    E a venda deve permanecer aguardando confirmacao de pagamento

  Cenario: PDV bloqueia pagamento Stone quando caixa nao possui terminal mapeado
    Dado que estou autenticado como operador do tenant "tenant-a"
    E que a filial ativa e "loja-a"
    E que existe uma sessao aberta no caixa "CAIXA-01" com pagamento eletronico habilitado
    E que nao existe terminal Stone mapeado para o caixa "CAIXA-01"
    Quando eu solicitar pagamento Stone por "DEBIT_CARD" no valor de "80.00"
    Entao a resposta do pedido Stone deve ter status HTTP 409
    E a resposta deve informar que o caixa nao possui terminal Stone configurado
    E nenhum pedido deve ser criado no provider Stone

  Cenario: PDV bloqueia pagamento Stone em sessao aberta como somente dinheiro
    Dado que estou autenticado como operador do tenant "tenant-a"
    E que a filial ativa e "loja-a"
    E que existe uma sessao aberta no caixa "CAIXA-01" em modo somente dinheiro
    E que o terminal Stone serial "6N021234" esta mapeado para o caixa "CAIXA-01"
    Quando eu solicitar pagamento Stone por "CREDIT_CARD" no valor de "50.00"
    Entao a resposta do pedido Stone deve ter status HTTP 409
    E a resposta deve informar que a sessao esta operando em modo somente dinheiro
    E nenhum pedido deve ser criado no provider Stone

  Cenario: Webhook charge.paid registra pagamento e fecha automaticamente pedido Stone
    Dado que estou autenticado como operador do tenant "tenant-a"
    E que existe uma venda aguardando pagamento Stone com externalReference "SESSION-CAIXA-01"
    E que existe um pedido Stone aberto com providerOrderId "or_stone_123"
    Quando a Stone enviar webhook valido "charge.paid" para o pedido "or_stone_123"
    Entao a resposta do webhook Stone deve ter status HTTP 200
    E a venda deve receber um pagamento com o metodo informado pelo webhook
    E o pedido Stone local deve ficar com status "APPROVED"
    E o backend deve solicitar fechamento do pedido Stone com status final "paid"

  Cenario: Webhook Stone com assinatura invalida nao altera venda nem pedido
    Dado que existe uma venda aguardando pagamento Stone com externalReference "SESSION-CAIXA-01"
    E que existe um pedido Stone aberto com providerOrderId "or_stone_123"
    Quando a Stone enviar webhook "charge.paid" com assinatura invalida para o pedido "or_stone_123"
    Entao a resposta do webhook Stone deve ter status HTTP 403
    E a venda nao deve receber pagamento
    E o pedido Stone local deve permanecer com status "PENDING"
    E o backend nao deve solicitar fechamento do pedido Stone

  Cenario: Webhook Stone de outro tenant nao pode confirmar venda do tenant atual
    Dado que existe uma venda do tenant "tenant-a" aguardando pagamento Stone com externalReference "SESSION-CAIXA-01"
    E que existe um pedido Stone do tenant "tenant-b" com providerOrderId "or_stone_tenant_b"
    Quando a Stone enviar webhook valido "charge.paid" para o pedido "or_stone_tenant_b"
    Entao a resposta do webhook Stone deve ter status HTTP 200
    E a venda do tenant "tenant-a" nao deve receber pagamento
    E o pedido Stone do tenant "tenant-b" deve ser atualizado isoladamente

