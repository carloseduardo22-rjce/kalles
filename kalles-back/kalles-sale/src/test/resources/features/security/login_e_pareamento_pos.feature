# language: pt
@security @auth @pos

Funcionalidade: Login web independente do pareamento do caixa
  Como ERP Kalles em ambiente web multi-tenant
  Quero que o login nao dependa do dispositivo em que e feito
  Para que o pareamento seja conferido na abertura e na operacao do caixa

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

  Cenario: Operador consegue logar sem posToken configurado no dispositivo
    Dado que o dispositivo nao possui o cookie "kalles_pos_token"
    Quando eu enviar o login com email "operador.caixa01@sistema.local" e senha "123456"
    Entao a resposta de login deve ter status HTTP 200
    E a resposta deve definir o cookie de autenticacao "kalles_auth_token"
