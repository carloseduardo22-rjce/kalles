# language: pt
@security @auth @pos

Funcionalidade: Login web e pareamento do dispositivo com o caixa
  Como ERP Kalles em ambiente web multi-tenant
  Quero distinguir o login administrativo do login operacional vinculado ao dispositivo
  Para permitir que administradores entrem sem bloqueio indevido e que operadores so acessem o caixa correto quando o terminal estiver previamente pareado

  Contexto:
    Dado que o seed "R__seed_data.sql" carregou a tenant "123e4567-e89b-12d3-a456-426614174000"
    E existe a empresa "Loja Matriz"
    E existe o caixa "CAIXA-01" para a empresa "Loja Matriz"
    E existe a conta web administradora "admin@sistema.local" com senha "123456"
    E existe a conta web operadora "operador.caixa01@sistema.local" com senha "123456" vinculada a empresa "Loja Matriz"

  Cenario: Administrador consegue logar sem posToken configurado no dispositivo
    Dado que o dispositivo nao possui o cookie "kalles_pos_token"
    Quando eu enviar o login com email "admin@sistema.local" e senha "123456"
    Entao a resposta de login deve ter status HTTP 200
    E a resposta deve definir o cookie de autenticacao "kalles_auth_token"
    E o login nao deve exigir pareamento de terminal para o perfil administrador

  Cenario: Operador nao consegue logar sem posToken configurado no dispositivo
    Dado que o dispositivo nao possui o cookie "kalles_pos_token"
    Quando eu enviar o login com email "operador.caixa01@sistema.local" e senha "123456"
    Entao a resposta de login deve ter status HTTP 400
    E a resposta deve conter a mensagem "Terminal nao configurado. Por favor, solicite o pareamento do caixa."

  Cenario: Administrador gera token de pareamento para um caixa da propria empresa
    Dado que estou autenticado como administrador da empresa "Loja Matriz"
    Quando eu solicitar a geracao de token de pareamento para o caixa "CAIXA-01"
    Entao a resposta deve ter status HTTP 200
    E a resposta deve conter um "pairingToken" preenchido
    E deve existir um pareamento de dispositivo pendente para o caixa "CAIXA-01"

  Cenario: Dispositivo realiza o pareamento usando token valido
    Dado que existe um token de pareamento ativo para o caixa "CAIXA-01"
    Quando o dispositivo enviar o token de pareamento valido
    Entao a resposta de pareamento deve ter status HTTP 200
    E a resposta deve conter a mensagem "Dispositivo pareado com sucesso."
    E a resposta deve definir o cookie "kalles_pos_token"
    E o dispositivo fica autorizado a operar o caixa "CAIXA-01" antes da abertura de sessao

  Cenario: Operador consegue logar com posToken valido do caixa da propria empresa
    Dado que o dispositivo possui um cookie "kalles_pos_token" valido para o caixa "CAIXA-01" da empresa "Loja Matriz"
    Quando eu enviar o login com email "operador.caixa01@sistema.local" e senha "123456"
    Entao a resposta de login deve ter status HTTP 200
    E a resposta deve definir o cookie de autenticacao "kalles_auth_token"
    E o token autenticado deve carregar o identificador do caixa pareado

  Cenario: Operador nao consegue logar com posToken revogado
    Dado que o dispositivo possui um cookie "kalles_pos_token" revogado para o caixa "CAIXA-01"
    Quando eu enviar o login com email "operador.caixa01@sistema.local" e senha "123456"
    Entao a resposta de login deve ter status HTTP 400
    E a resposta deve conter a mensagem "Sessao do terminal invalida ou expirada."

  Cenario: Operador nao consegue logar com posToken de outra empresa
    Dado que o dispositivo possui um cookie "kalles_pos_token" valido para um caixa de outra empresa
    Quando eu enviar o login com email "operador.caixa01@sistema.local" e senha "123456"
    Entao a resposta de login deve ter status HTTP 400
    E a resposta deve conter a mensagem "Este terminal nao pertence a filial do caixa."

  Cenario: Operador nao pode abrir sessao de caixa sem dispositivo previamente pareado
    Dado que o dispositivo nao possui o cookie "kalles_pos_token"
    E que o operador esta autenticado
    Quando ele tentar abrir sessao no caixa "CAIXA-01"
    Entao a resposta da abertura deve ter status HTTP 400
    E a resposta deve indicar que o dispositivo precisa estar pareado antes da operacao

  Cenario: Pareamento falha quando o token informado esta invalido
    Dado que o dispositivo nao possui o cookie "kalles_pos_token"
    Quando o dispositivo enviar um token de pareamento invalido
    Entao a resposta de pareamento deve ter status HTTP 400
    E a resposta deve conter a mensagem "Token de pareamento invalido ou expirado."

  Cenario: Geracao de token exige companyId e posId
    Dado que estou autenticado como administrador da empresa "Loja Matriz"
    Quando eu solicitar a geracao de token de pareamento sem informar companyId ou posId
    Entao a resposta deve ter status HTTP 400
    E a resposta deve conter a mensagem "Parametros companyId e posId sao obrigatorios."
