# language: pt
@cashregister @RF-002

Funcionalidade: Pareamento do dispositivo com o caixa
  Como administrador da loja
  Quero parear um navegador com um caixa por um codigo de uso unico
  Para que so o dispositivo autorizado abra e opere a sessao daquele caixa

  Contexto:
    Dado um administrador autenticado na loja do caixa

  @CA-002.1
  Cenario: Administrador gera o codigo de pareamento com prazo de validade
    Quando o administrador gerar o codigo de pareamento do caixa
    Entao a resposta do pareamento deve ter status HTTP 201
    E o codigo gerado deve ter 8 caracteres
    E o codigo gerado deve vencer em 10 minutos

  @CA-002.2
  Cenario: Codigo valido torna o navegador o dispositivo do caixa
    Dado um codigo de pareamento gerado para o caixa
    Quando o dispositivo "A" informar o codigo de pareamento
    Entao a resposta do pareamento deve ter status HTTP 200
    E o dispositivo "A" deve estar pareado com o caixa
    E o dispositivo "A" deve conseguir abrir a sessao do caixa

  @CA-002.3
  Esquema do Cenario: Codigo usado, vencido ou inexistente e recusado
    Dado um codigo de pareamento <situacao>
    Quando o dispositivo "B" informar o codigo de pareamento
    Entao a resposta do pareamento deve ter status HTTP 422
    E o erro do pareamento deve ter o codigo "PAIRING_CODE_INVALID"

    Exemplos:
      | situacao    |
      | ja usado    |
      | vencido     |
      | inexistente |

  @CA-002.4
  Cenario: Novo pareamento revoga o dispositivo anterior
    Dado o dispositivo "A" pareado com o caixa
    E o dispositivo "B" pareado com o caixa
    Quando o dispositivo "A" tentar abrir a sessao do caixa
    Entao a resposta do pareamento deve ter status HTTP 403
    E o erro do pareamento deve ter o codigo "DEVICE_NOT_PAIRED"
    E o dispositivo "B" deve conseguir abrir a sessao do caixa

  @CA-002.5
  Cenario: Pareamento revogado nao opera nem abre sessao
    Dado o dispositivo "A" pareado com o caixa
    E o dispositivo "A" com a sessao do caixa aberta
    Quando o administrador revogar o pareamento do caixa
    Entao a resposta do pareamento deve ter status HTTP 204
    E o dispositivo "A" nao deve conseguir registrar venda na sessao aberta
    E o dispositivo "A" nao deve conseguir abrir a sessao do caixa

  @CA-002.6
  Cenario: Navegador nao pareado nao abre sessao
    Quando o dispositivo "C" tentar abrir a sessao do caixa
    Entao a resposta do pareamento deve ter status HTTP 403
    E o erro do pareamento deve ter o codigo "DEVICE_NOT_PAIRED"

  @CA-002.15
  Cenario: Pareamento antigo continua valido sem revogacao
    Dado o dispositivo "A" pareado com o caixa ha 8 dias
    Quando o dispositivo "A" tentar abrir a sessao do caixa
    Entao a resposta do pareamento deve ter status HTTP 201
