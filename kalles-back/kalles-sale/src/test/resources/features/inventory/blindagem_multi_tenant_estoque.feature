# language: pt
@inventory @multi_tenant

Funcionalidade: Blindagem multi-tenant de inventory
  Como admin autenticado no ERP
  Quero manipular produtos, depositos e estoque somente no meu contexto valido
  Para impedir vazamento de tenant e filial

  Cenario: Filial ativa obrigatoria para listar depositos
    Dado um admin autenticado do tenant atual
    Quando ele listar depositos sem informar a filial ativa
    Entao a resposta de inventory deve ter status HTTP 400
    E a resposta deve informar que a filial ativa e obrigatoria para inventory

  Cenario: Produto criado na filial ativa respeita o tenant atual
    Dado um admin autenticado do tenant atual
    Quando ele cadastrar um produto na filial ativa
    Entao a resposta de inventory deve ter status HTTP 201
    E o produto criado deve retornar o codigo interno "ARZ-001"

  Cenario: Deposito de outra filial nao pode ser consultado
    Dado um admin autenticado do tenant atual
    E um deposito cadastrado em outra filial do mesmo tenant
    Quando ele consultar o deposito externo no contexto da filial ativa
    Entao a resposta de inventory deve ter status HTTP 404

  Cenario: Produto de outro tenant nao pode receber estoque na filial ativa
    Dado um admin autenticado do tenant atual
    E um produto cadastrado em outro tenant
    E uma localizacao cadastrada na filial ativa
    Quando ele tentar registrar estoque para o produto externo
    Entao a resposta de inventory deve ter status HTTP 404
