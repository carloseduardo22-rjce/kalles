# language: pt
@report @multi_tenant

Funcionalidade: Relatorio financeiro por filial
  Como admin autenticado no ERP
  Quero consultar relatorios financeiros apenas dentro da filial ativa
  Para impedir cruzamento de indicadores entre filiais

  Cenario: Relatorio financeiro da filial ativa
    Dado um admin autenticado para reports
    E dados financeiros cadastrados em filiais diferentes
    Quando ele consultar o relatorio financeiro da filial ativa
    Entao a resposta de report deve ter status HTTP 200
    E o relatorio deve retornar apenas os valores da filial ativa

  Cenario: Filial ativa obrigatoria para consultar relatorio financeiro
    Dado um admin autenticado para reports
    Quando ele consultar o relatorio financeiro sem informar a filial ativa
    Entao a resposta de report deve ter status HTTP 400
    E a resposta deve informar que a filial ativa e obrigatoria para reports
