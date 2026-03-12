# language: pt

Funcionalidade: Metas de Faturamento
  Como gestor do ERP Kalles
  Eu quero configurar metas globais de faturamento por período
  Para acompanhar o desempenho financeiro da empresa e identificar lacunas em relação ao objetivo

  #===========================================================================
  # CENÁRIO 1: CRIAÇÃO COM SUCESSO
  #===========================================================================

  Cenário: Criação de uma Meta Mensal com sucesso
    Dado que nao existe nenhuma Meta com Periodicidade "MONTHLY" ativa entre "2026-03-01" e "2026-03-31"
    Quando o gestor cria uma Meta com os seguintes dados:
      | valorAlvo     | 100000.00  |
      | periodicidade | MONTHLY    |
      | dataInicio    | 2026-03-01 |
      | dataFim       | 2026-03-31 |
    Então a criacao da Meta deve ser bem-sucedida
    E o status da Meta deve ser "DRAFT"
    E o valor alvo da Meta deve ser "100000.00"

  #===========================================================================
  # CENÁRIO 2: RECUSA POR SOBREPOSIÇÃO DE PERÍODO ATIVO
  #===========================================================================

  Cenário: Recusa ao tentar criar uma Meta que sobrepõe um período já ativo
    Dado que existe uma Meta com Periodicidade "MONTHLY" e status "ACTIVE" entre "2026-03-01" e "2026-03-31"
    Quando o gestor tenta criar uma nova Meta com os seguintes dados:
      | valorAlvo     | 120000.00  |
      | periodicidade | MONTHLY    |
      | dataInicio    | 2026-03-15 |
      | dataFim       | 2026-04-15 |
    Então a criacao da Meta deve ser recusada
    E a mensagem de erro da Meta deve ser "There is already an active MONTHLY Goal overlapping the given period"

  #===========================================================================
  # CENÁRIO 3: CÁLCULO DE APURAÇÃO
  #===========================================================================

  Cenário: Apuração da Meta com vendas realizadas no período
    Dado que existe uma Meta com Periodicidade "MONTHLY" e status "ACTIVE" com valor alvo de "100000.00" entre "2026-03-01" e "2026-03-31"
    E que as seguintes vendas foram concluidas no periodo:
      | dataVenda  | valorTotal |
      | 2026-03-05 | 20000.00   |
      | 2026-03-12 | 35000.00   |
      | 2026-03-20 | 15000.00   |
    Quando o gestor solicita a apuracao da Meta
    Então o valor realizado deve ser "70000.00"
    E a lacuna deve ser "30000.00"
