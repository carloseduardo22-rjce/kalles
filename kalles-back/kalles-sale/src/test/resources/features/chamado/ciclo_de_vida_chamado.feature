# language: pt

Funcionalidade: Ciclo de Vida de um Chamado de Suporte
  Como um usuário cliente do ERP Kalles
  Eu quero poder abrir um chamado registrando meu problema com detalhes precisos
  Para que a equipe de suporte possa rastreá-lo e resolvê-lo com um SLA definido

  #===========================================================================
  # CONTEXTO COMPARTILHADO
  #===========================================================================
  Contexto:
    Dado que existe uma regra de negócio onde a categoria "Sistema" e subcategoria "Bug" gera prioridade "HIGH"
    E que existe um usuário com email "joao.silva@cliente.com" e nome "João Silva"
    E que existe um atendente com matrícula "ATD-001" e nome "Carlos Dev"

  #===========================================================================
  # BLOCO 1: ABERTURA DE UM NOVO CHAMADO
  #===========================================================================

  Cenário: Abertura bem-sucedida de um chamado de Bug com prioridade automática
    Quando o usuário "joao.silva@cliente.com" abre um novo chamado com os seguintes dados:
      | titulo       | Erro 500 ao acessar o módulo de Vendas (PDV)              |
      | descricao    | Ao tentar autenticar no PDV recebo erro interno do servidor |
      | categoria    | Sistema                                                    |
      | subcategoria | Bug                                                        |
    Então um chamado deve ser criado com sucesso
    E o chamado deve ter o status "OPEN"
    E o chamado deve estar associado ao usuário "joao.silva@cliente.com"
    E o sistema deve atribuir automaticamente a prioridade "HIGH" devido à classificação "Sistema / Bug"
    E o chamado não deve ter nenhum atendente atribuído
    E o SLA do chamado deve estar ativo com o contador de tempo iniciado

  Cenário: Recusa ao abrir um chamado sem título
    Quando o usuário "joao.silva@cliente.com" tenta abrir um chamado sem informar o título
    Então o sistema deve recusar a operação
    E a mensagem de erro deve ser "Ticket title is required"

  Cenário: Recusa ao abrir um chamado sem categoria
    Quando o usuário "joao.silva@cliente.com" tenta abrir um chamado sem informar a categoria
    Então o sistema deve recusar a operação
    E a mensagem de erro deve ser "Ticket category is required"

  Cenário: Recusa ao abrir um chamado sem descrição
    Quando o usuário "joao.silva@cliente.com" tenta abrir um chamado sem informar a descrição
    Então o sistema deve recusar a operação
    E a mensagem de erro deve ser "Ticket description is required"

  #===========================================================================
  # BLOCO 2: TRANSIÇÃO DE ESTADO — ABERTO → EM ANDAMENTO
  #===========================================================================

  Cenário: Atendente assume um chamado com status 'Aberto' com sucesso
    Dado que existe um chamado de id "CH-001" com status "OPEN" pertencente ao usuário "joao.silva@cliente.com"
    Quando o atendente "ATD-001" assume o chamado "CH-001"
    Então o status do chamado "CH-001" deve ser alterado para "IN_PROGRESS"
    E o atendente "ATD-001" deve ser atribuído como responsável pelo chamado "CH-001"
    E uma nota interna deve ser registrada automaticamente no chamado "CH-001" com o texto "Ticket assigned to agent Carlos Dev"

  Cenário: Recusa ao assumir um chamado que já está 'Em Andamento'
    Dado que existe um chamado de id "CH-002" com status "IN_PROGRESS" sob responsabilidade do atendente "ATD-001"
    Quando um segundo atendente com matrícula "ATD-002" tenta assumir o chamado "CH-002"
    Então o sistema deve recusar a operação
    E a mensagem de erro deve ser "Invalid state transition: the ticket is already in progress"

  Cenário: Recusa ao assumir um chamado com status 'Fechado'
    Dado que existe um chamado de id "CH-003" com status "CLOSED"
    Quando o atendente "ATD-001" tenta assumir o chamado "CH-003"
    Então o sistema deve recusar a operação
    E a mensagem de erro deve ser "Invalid state transition: closed tickets cannot be reopened through this flow"

  Cenário: Recusa ao assumir um chamado com status 'Resolvido'
    Dado que existe um chamado de id "CH-004" com status "RESOLVED"
    Quando o atendente "ATD-001" tenta assumir o chamado "CH-004"
    Então o sistema deve recusar a operação
    E a mensagem de erro deve ser "Invalid state transition: the ticket has already been resolved"
