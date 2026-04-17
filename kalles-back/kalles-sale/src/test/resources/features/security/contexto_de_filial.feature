# language: pt
@security @company_context

Funcionalidade: Contexto de filial em rotas escopadas
  Como usuario autenticado no ERP
  Quero acessar dados apenas dentro do contexto de filial permitido
  Para preservar o isolamento entre filiais e tenants

  Cenario: Admin do tenant precisa informar a filial ao acessar rota escopada
    Dado um admin autenticado sem filial fixa no token
    Quando ele consultar os caixas sem informar a filial ativa
    Entao a resposta de contexto de filial deve ter status HTTP 400
    E a resposta deve informar que a filial ativa e obrigatoria

  Cenario: Admin do tenant pode acessar rota escopada com uma filial do proprio tenant
    Dado um admin autenticado sem filial fixa no token
    Quando ele consultar os caixas informando uma filial acessivel do proprio tenant
    Entao a resposta de contexto de filial deve ter status HTTP 200
    E a lista de caixas deve conter apenas os caixas da filial informada

  Cenario: Usuario com filial fixa nao pode sobrescrever o header de filial
    Dado um admin autenticado com filial fixa no token
    Quando ele consultar os caixas informando outra filial no header
    Entao a resposta de contexto de filial deve ter status HTTP 403
    E a resposta deve informar que o contexto de filial foi negado
