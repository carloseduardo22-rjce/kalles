# language: pt
@fidelity_policy @multi_tenant

Funcionalidade: Gestao de politica de fidelidade por filial
  Como admin autenticado no ERP
  Quero gerenciar a politica de fidelidade apenas dentro da filial ativa
  Para impedir cruzamento de beneficios entre filiais

  Cenario: Politica criada na filial ativa
    Dado um admin autenticado para fidelity policy
    Quando ele cadastrar uma politica de fidelidade na filial ativa
    Entao a resposta de fidelity policy deve ter status HTTP 201
    E a politica criada deve retornar o objetivo de pontos 200

  Cenario: Filial ativa obrigatoria para consultar politica ativa
    Dado um admin autenticado para fidelity policy
    Quando ele consultar a politica ativa sem informar a filial ativa
    Entao a resposta de fidelity policy deve ter status HTTP 400
    E a resposta deve informar que a filial ativa e obrigatoria para fidelity policy

  Cenario: Listagem nao deve incluir politica de outra filial
    Dado um admin autenticado para fidelity policy
    E uma politica ativa cadastrada em outra filial
    Quando ele listar as politicas no contexto da filial ativa
    Entao a resposta de fidelity policy deve ter status HTTP 200
    E a listagem nao deve incluir a politica da outra filial
