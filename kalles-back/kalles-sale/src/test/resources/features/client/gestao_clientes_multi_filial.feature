# language: pt
@client @multi_tenant

Funcionalidade: Gestao de clientes por filial
  Como admin autenticado no ERP
  Quero cadastrar e consultar clientes apenas dentro da filial ativa
  Para impedir cruzamento administrativo entre filiais

  Cenario: Cliente criado na filial ativa
    Dado um admin autenticado para clients
    Quando ele cadastrar um cliente na filial ativa
    Entao a resposta de client deve ter status HTTP 201
    E o cliente criado deve retornar o CPF "52998224725"

  Cenario: Filial ativa obrigatoria para listar clientes
    Dado um admin autenticado para clients
    Quando ele listar clientes sem informar a filial ativa
    Entao a resposta de client deve ter status HTTP 400
    E a resposta deve informar que a filial ativa e obrigatoria para clients

  Cenario: Cliente de outra filial nao pode ser consultado
    Dado um admin autenticado para clients
    E um cliente cadastrado em outra filial
    Quando ele consultar o cliente externo no contexto da filial ativa
    Entao a resposta de client deve ter status HTTP 404
