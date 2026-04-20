# language: pt
@operator @multi_tenant

Funcionalidade: Gestao de operadores por filial
  Como admin autenticado no ERP
  Quero cadastrar e consultar operadores apenas dentro da filial ativa
  Para impedir cruzamento administrativo entre filiais

  Cenario: Operador criado na filial ativa
    Dado um admin autenticado para operators
    Quando ele cadastrar um operador na filial ativa
    Entao a resposta de operator deve ter status HTTP 201
    E o operador criado deve retornar o codigo "maria.silva"

  Cenario: Filial ativa obrigatoria para listar operadores
    Dado um admin autenticado para operators
    Quando ele listar operadores sem informar a filial ativa
    Entao a resposta de operator deve ter status HTTP 400
    E a resposta deve informar que a filial ativa e obrigatoria para operators

  Cenario: Operador de outra filial nao pode ser consultado
    Dado um admin autenticado para operators
    E um operador cadastrado em outra filial
    Quando ele consultar o operador externo no contexto da filial ativa
    Entao a resposta de operator deve ter status HTTP 404
