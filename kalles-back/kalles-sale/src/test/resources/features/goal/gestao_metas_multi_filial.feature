# language: pt
@goal @multi_tenant

Funcionalidade: Gestao de metas por filial
  Como admin autenticado no ERP
  Quero gerenciar metas apenas dentro da filial ativa
  Para impedir cruzamento administrativo entre filiais

  Cenario: Meta criada na filial ativa
    Dado um admin autenticado para goals
    Quando ele cadastrar uma meta na filial ativa
    Entao a resposta de goal deve ter status HTTP 201
    E a meta criada deve retornar o status "DRAFT"

  Cenario: Filial ativa obrigatoria para listar metas
    Dado um admin autenticado para goals
    Quando ele listar metas sem informar a filial ativa
    Entao a resposta de goal deve ter status HTTP 400
    E a resposta deve informar que a filial ativa e obrigatoria para goals

  Cenario: Meta de outra filial nao pode ser consultada
    Dado um admin autenticado para goals
    E uma meta ativa cadastrada em outra filial
    Quando ele consultar a meta externa no contexto da filial ativa
    Entao a resposta de goal deve ter status HTTP 404
