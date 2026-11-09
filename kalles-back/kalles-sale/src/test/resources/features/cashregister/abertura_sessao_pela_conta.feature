# language: pt
@cashregister @RF-002 @contaDaSessao

Funcionalidade: Abertura da sessao em nome da conta autenticada
  Como operador de caixa
  Quero que a sessao seja aberta em meu nome ao entrar no caixa
  Para que cada venda fique registrada em nome de quem realmente operou

  @CA-002.7
  Cenario: Operador abre a sessao em nome proprio
    Dado um operador com cadastro autenticado no dispositivo pareado
    Quando a pessoa autenticada abrir a sessao do caixa
    Entao a abertura pela conta deve ter status HTTP 201
    E a sessao deve estar em nome de "Operador Caixa 01"

  @CA-002.8
  Cenario: Administrador sem cadastro de operador abre a sessao em nome proprio
    Dado um administrador sem cadastro de operador autenticado no dispositivo pareado
    Quando a pessoa autenticada abrir a sessao do caixa
    Entao a abertura pela conta deve ter status HTTP 201
    E a sessao deve estar em nome de "Administrador da loja"
    E o administrador deve ter recebido a matricula "adm-1"

  @CA-002.10
  Cenario: Operador com sessao aberta em outro caixa nao abre uma segunda
    Dado um operador com cadastro autenticado no dispositivo pareado
    E o operador ja tem sessao aberta em outro caixa
    Quando a pessoa autenticada abrir a sessao do caixa
    Entao a abertura pela conta deve ter status HTTP 409
    E o erro da abertura deve ter o codigo "OPERATOR_ALREADY_IN_SESSION"

  @CA-002.16
  Cenario: Conta de operador sem cadastro nao abre sessao
    Dado uma conta de operador sem cadastro autenticada no dispositivo pareado
    Quando a pessoa autenticada abrir a sessao do caixa
    Entao a abertura pela conta deve ter status HTTP 422
    E o erro da abertura deve ter o codigo "OPERATOR_ACCOUNT_NOT_LINKED"
