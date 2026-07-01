# language: pt
@fiscal @onboarding @salvamento_unificado @multi_tenant @wip

Funcionalidade: Onboarding e salvamento unificado da prontidao fiscal
  Como administrador do ERP Kalles
  Quero receber orientacao clara sobre a configuracao fiscal da loja e salvar os dados relacionados em um unico fluxo
  Para preparar a filial para emitir NFC-e com menos risco de configuracao parcial ou confusa

  Contexto:
    Dado que existe um administrador autenticado no tenant atual
    E existe uma filial ativa no tenant atual

  Cenario: Exibir onboarding rico da tela fiscal para o primeiro acesso
    Quando o administrador acessar a tela fiscal da filial ativa pela primeira vez
    Entao o guia da tela deve iniciar automaticamente
    E o guia deve explicar o papel da tela fiscal
    E o guia deve explicar a empresa emissora
    E o guia deve explicar o endereco fiscal
    E o guia deve explicar numeracao, CSC e certificado A1
    E o guia deve explicar a tributacao dos produtos
    E o guia deve explicar emissao, acompanhamento de nota e devolucao
    E nenhum texto do guia deve mencionar DTO, backend, frontend, API, porta, adapter ou arquitetura interna

  Cenario: Permitir rever o onboarding fiscal depois de concluido
    Dado que o administrador concluiu o guia da tela fiscal
    Quando o administrador clicar em rever guia da tela
    Entao o guia fiscal deve ser exibido novamente
    E o estado de conclusao do guia deve permanecer vinculado ao navegador do usuario

  Cenario: Salvar preparacao fiscal da loja em uma unica acao
    Quando o administrador preencher os dados fiscais da loja com:
      | campo              | valor                      |
      | cnpj               | 11222333000181             |
      | razaoSocial        | Kalles Comercio Teste LTDA |
      | nomeFantasia       | Kalles Loja Teste          |
      | inscricaoEstadual  | 110042490114               |
      | regimeTributario   | SIMPLES_NACIONAL           |
      | cnae               | 4712100                    |
      | cep                | 01001000                   |
      | uf                 | SP                         |
      | codigoIbgeUf       | 35                         |
      | municipio          | Sao Paulo                  |
      | codigoIbgeMunicipio| 3550308                    |
      | bairro             | Se                         |
      | logradouro         | Praca da Se                |
      | numero             | 100                        |
      | ambiente           | HOMOLOGACAO                |
      | cscId              | 1                          |
      | cscToken           | CSC-HOMOLOGACAO            |
      | serie              | 1                          |
      | proximoNumero      | 100                        |
      | certificadoA1      | BASE64-PFX                 |
      | senhaCertificado   | senha-segura               |
      | validadeCertificado| 2027-04-30T13:00:00Z       |
    E clicar em salvar preparacao fiscal
    Entao a resposta fiscal deve ter status HTTP 201
    E a preparacao fiscal deve pertencer ao tenant atual
    E a preparacao fiscal deve pertencer a filial ativa
    E a filial deve ser marcada como pronta quando todos os dados obrigatorios estiverem validos
    E a resposta nao deve expor senha, certificado, CSC token ou qualquer segredo fiscal sensivel

  Cenario: Bloquear salvamento unificado quando algum dado obrigatorio da loja estiver invalido
    Quando o administrador tentar salvar preparacao fiscal com CNPJ invalido
    Entao a resposta fiscal deve ter status HTTP 400
    E a resposta fiscal deve informar "CNPJ do emissor fiscal invalido"
    E nenhum dado fiscal parcial deve marcar a filial como pronta para emissao

  Cenario: Bloquear salvamento unificado com certificado vencido
    Quando o administrador tentar salvar preparacao fiscal com certificado A1 vencido
    Entao a resposta fiscal deve ter status HTTP 400
    E a resposta fiscal deve informar "Certificado digital expirado"
    E nenhum certificado vencido deve ficar ativo para a filial

  Cenario: Manter classificacao fiscal de produto como fluxo proprio da tela
    Dado que a loja possui preparacao fiscal salva
    E existe um produto do tenant atual
    Quando o administrador salvar a tributacao fiscal desse produto
    Entao a resposta fiscal deve ter status HTTP 201
    E a classificacao deve pertencer ao tenant atual
    E a classificacao deve pertencer a filial ativa
    E a classificacao deve pertencer ao produto informado

  Cenario: Isolar preparacao fiscal unificada entre tenants
    Dado que existe uma filial fiscalmente preparada em outro tenant
    Quando o administrador do tenant atual consultar a prontidao fiscal da filial ativa
    Entao os dados fiscais do outro tenant nao devem ser retornados
    E a resposta deve considerar apenas a filial ativa do tenant atual

  Cenario: Isolar preparacao fiscal unificada entre filiais do mesmo tenant
    Dado que existe uma filial fiscalmente preparada em outra filial do mesmo tenant
    Quando o administrador consultar a prontidao fiscal usando a filial ativa atual
    Entao os dados fiscais da outra filial nao devem ser retornados
    E a resposta deve considerar apenas a filial ativa atual
