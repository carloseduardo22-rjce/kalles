# language: pt
@cashregister

Funcionalidade: Abertura de sessao de caixa sem integracao de pagamento
  Como administrador do PDV
  Quero decidir conscientemente quando um caixa operara somente em dinheiro
  Para manter a operacao ativa sem permitir PIX, vouchers ou cartoes quando nao houver integracao configurada

  Cenario: Abrir sessao normalmente quando o pagamento esta configurado
    Dado que o caixa possui integracao de pagamento configurada
    E um operador autenticado deseja abrir a sessao do caixa com valor inicial "100.00"
    Quando solicitar a abertura da sessao
    Entao a resposta da abertura deve ter status HTTP 201
    E a sessao retornada nao deve estar em modo somente dinheiro

  Cenario: Bloquear abertura sem configuracao de pagamento e sem confirmacao
    Dado que o caixa nao possui integracao de pagamento configurada
    E um operador autenticado deseja abrir a sessao do caixa com valor inicial "100.00"
    Quando solicitar a abertura da sessao
    Entao a resposta da abertura deve ter status HTTP 409
    E a resposta deve informar "Pagamento nao configurado, neste caixa voce apenas podera operar com dinheiro mas nao podera receber pagamentos via pix, vouchers e cartoes de credito."

  Cenario: Permitir abertura em modo somente dinheiro quando houver confirmacao explicita
    Dado que o caixa nao possui integracao de pagamento configurada
    E um operador autenticado deseja abrir a sessao do caixa com valor inicial "100.00"
    E o operador confirma a abertura em modo somente dinheiro
    Quando solicitar a abertura da sessao
    Entao a resposta da abertura deve ter status HTTP 201
    E a sessao retornada deve estar em modo somente dinheiro
