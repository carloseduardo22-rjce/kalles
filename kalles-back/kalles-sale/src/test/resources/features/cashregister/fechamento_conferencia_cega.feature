# language: pt
@cashregister @RF-002 @fechamentoCego

Funcionalidade: Fechamento da sessao por conferencia cega
  Como gerente da loja
  Quero que o operador informe o dinheiro contado sem ver o valor esperado
  Para que cada diferenca de caixa seja real e fique registrada

  Contexto:
    Dado um operador com sessao aberta e fundo de troco de "100.00"

  @CA-002.11
  Esquema do Cenario: Operador nao consulta o valor esperado com a sessao aberta
    Quando o operador consultar <consulta> com a sessao aberta
    Entao a consulta deve ter status HTTP 403

    Exemplos:
      | consulta      |
      | os detalhes   |
      | o resumo      |
      | a lista       |

  @CA-002.11
  Cenario: Administrador acompanha o valor esperado da sessao aberta
    Dado um administrador autenticado na loja
    Quando o administrador consultar o resumo da sessao aberta
    Entao a consulta deve ter status HTTP 200
    E o saldo esperado deve ser "100.00"

  @CA-002.12
  Cenario: Diferenca dentro da tolerancia fecha sem liberacao
    Dado a tolerancia de caixa da loja de "5.00"
    Quando o operador informar o valor contado de "97.00"
    Entao o fechamento deve ter status HTTP 200
    E a diferenca registrada deve ser "-3.00"
    E o fechamento nao deve ter autorizador
    E a sessao deve estar fechada

  @CA-002.12
  Cenario: Diferenca igual a tolerancia fecha sem liberacao
    Dado a tolerancia de caixa da loja de "5.00"
    Quando o operador informar o valor contado de "105.00"
    Entao o fechamento deve ter status HTTP 200
    E a diferenca registrada deve ser "5.00"
    E o fechamento nao deve ter autorizador
    E a sessao deve estar fechada

  @CA-002.12
  Cenario: Operador ve a diferenca depois de fechar
    Dado a tolerancia de caixa da loja de "5.00"
    E o operador informar o valor contado de "97.00"
    Quando o operador consultar os detalhes da sessao fechada
    Entao a consulta deve ter status HTTP 200
    E a diferenca registrada deve ser "-3.00"

  @CA-002.13
  Cenario: Diferenca acima da tolerancia pede liberacao sem revelar valores
    Dado a tolerancia de caixa da loja de "5.00"
    Quando o operador informar o valor contado de "105.01"
    Entao o fechamento deve ter status HTTP 422
    E o erro do fechamento deve ter o codigo "CLOSING_AUTHORIZATION_REQUIRED"
    E a resposta nao deve revelar o valor esperado
    E a sessao deve continuar aberta
    E a primeira contagem divergente deve ser "105.01"

  @CA-002.13
  Cenario: Loja nova nao tolera diferenca alguma
    Quando o operador informar o valor contado de "100.01"
    Entao o fechamento deve ter status HTTP 422
    E o erro do fechamento deve ter o codigo "CLOSING_AUTHORIZATION_REQUIRED"

  @CA-002.13
  Cenario: Supervisor libera o fechamento acima da tolerancia
    Dado um supervisor cadastrado com matricula "SUP01"
    E o operador informar o valor contado de "90.00"
    Quando o operador informar o valor contado de "90.00" com a liberacao de "SUP01"
    Entao o fechamento deve ter status HTTP 200
    E a diferenca registrada deve ser "-10.00"
    E o fechamento deve ter sido liberado por "SUP01"
    E a sessao deve estar fechada

  @CA-002.13
  Cenario: Recontagem com outro valor fecha pelo valor liberado e guarda a primeira contagem
    Dado um supervisor cadastrado com matricula "SUP01"
    E o operador informar o valor contado de "90.00"
    Quando o operador informar o valor contado de "95.00" com a liberacao de "SUP01"
    Entao o fechamento deve ter status HTTP 200
    E a diferenca registrada deve ser "-5.00"
    E o fechamento deve ter sido liberado por "SUP01"
    E a primeira contagem divergente deve ser "90.00"

  @CA-002.13
  Cenario: Recontagem depois da recusa continua exigindo liberacao
    Dado o operador informar o valor contado de "90.00"
    Quando o operador informar o valor contado de "100.00"
    Entao o fechamento deve ter status HTTP 422
    E o erro do fechamento deve ter o codigo "CLOSING_AUTHORIZATION_REQUIRED"
    E a primeira contagem divergente deve ser "90.00"

  @CA-002.13
  Cenario: Pessoa sem nivel de supervisor nao libera o fechamento
    Dado um operador de nivel basico cadastrado com matricula "OP099"
    Quando o operador informar o valor contado de "90.00" com a liberacao de "OP099"
    Entao o fechamento deve ter status HTTP 403
    E o erro do fechamento deve ter o codigo "CLOSING_AUTHORIZER_NOT_ALLOWED"
    E a sessao deve continuar aberta
    E a primeira contagem divergente deve ser "90.00"

  Cenario: Quem tem nivel de supervisor fecha a propria sessao com diferenca
    Dado o operador da sessao tem nivel de supervisor
    Quando o operador informar o valor contado de "90.00"
    Entao o fechamento deve ter status HTTP 200
    E a diferenca registrada deve ser "-10.00"
    E o fechamento nao deve ter autorizador

  Cenario: Valor contado acima do limite e recusado sem registrar contagem
    Quando o operador informar o valor contado de "100000000000000000.00"
    Entao o fechamento deve ter status HTTP 400
    E a sessao deve continuar aberta

  Cenario: Administrador configura a tolerancia da loja
    Dado um administrador autenticado na loja
    Quando o administrador definir a tolerancia da loja em "12.50"
    Entao a configuracao deve ter status HTTP 200
    E a tolerancia da loja deve ser "12.50"

  Esquema do Cenario: Tolerancia invalida e recusada
    Dado um administrador autenticado na loja
    Quando o administrador definir a tolerancia da loja em "<valor>"
    Entao a configuracao deve ter status HTTP 400
    E a tolerancia da loja deve ser "0.00"

    Exemplos:
      | valor |
      | -1.00 |
      | 1.234 |
      | null  |

  Cenario: Tolerancia zero e aceita
    Dado um administrador autenticado na loja
    Quando o administrador definir a tolerancia da loja em "0.00"
    Entao a configuracao deve ter status HTTP 200
    E a tolerancia da loja deve ser "0.00"

  Cenario: Administrador nao altera a tolerancia de loja de outro tenant
    Dado um administrador autenticado na loja
    E uma loja de outro tenant
    Quando o administrador definir a tolerancia da loja de outro tenant em "9.00"
    Entao a configuracao deve ter status HTTP 404
    E a tolerancia da loja de outro tenant deve continuar "0.00"

  Cenario: Operador nao altera a tolerancia
    Quando o operador definir a tolerancia da loja em "50.00"
    Entao a configuracao deve ter status HTTP 403

  Cenario: Operador nao consulta a tolerancia
    Quando o operador consultar a tolerancia da loja
    Entao a consulta deve ter status HTTP 403
