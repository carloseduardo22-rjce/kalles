# language: pt
@fiscal @prontidao_fiscal @multi_tenant @wip

Funcionalidade: Prontidao fiscal da filial para emissao real
  Como administrador do ERP Kalles
  Quero cadastrar e validar os dados fiscais completos da filial emissora
  Para saber se a loja esta pronta para emitir NFC-e/NF-e real sem depender de dados externos ocultos

  Contexto:
    Dado que existe um administrador autenticado no tenant atual
    E existe uma filial ativa no tenant atual

  Cenario: Cadastrar identificacao fiscal completa da filial emissora
    Quando o administrador salvar os dados fiscais da filial com:
      | campo             | valor                         |
      | cnpj              | 11222333000181                |
      | razaoSocial       | Kalles Comercio Teste LTDA    |
      | nomeFantasia      | Kalles Loja Teste             |
      | inscricaoEstadual | 110042490114                  |
      | regimeTributario  | SIMPLES_NACIONAL              |
      | cnae              | 4712100                       |
    Entao a resposta fiscal deve ter status HTTP 201
    E os dados fiscais devem pertencer ao tenant atual
    E os dados fiscais devem pertencer a filial ativa
    E a resposta nao deve expor nenhum segredo fiscal sensivel

  Cenario: Cadastrar endereco fiscal completo da filial emissora
    Quando o administrador salvar o endereco fiscal da filial com:
      | campo        | valor              |
      | cep          | 01001000           |
      | uf           | SP                 |
      | codigoIbgeUf | 35                 |
      | municipio    | Sao Paulo          |
      | codigoIbgeMunicipio | 3550308     |
      | bairro       | Se                 |
      | logradouro   | Praca da Se        |
      | numero       | 100                |
      | pais         | Brasil             |
      | codigoPais   | 1058               |
    Entao a resposta fiscal deve ter status HTTP 201
    E o endereco fiscal deve pertencer ao tenant atual
    E o endereco fiscal deve pertencer a filial ativa

  Cenario: Bloquear filial fiscal sem CNPJ valido
    Quando o administrador tentar salvar dados fiscais da filial com CNPJ invalido
    Entao a resposta fiscal deve ter status HTTP 400
    E a resposta fiscal deve informar "CNPJ do emissor fiscal invalido"
    E a filial nao deve ser marcada como pronta para emissao fiscal

  Cenario: Bloquear filial fiscal sem inscricao estadual quando o modelo exigir
    Quando o administrador tentar habilitar NFC-e sem inscricao estadual
    Entao a resposta fiscal deve ter status HTTP 400
    E a resposta fiscal deve informar "Inscricao estadual e obrigatoria para NFC-e"
    E a filial nao deve ser marcada como pronta para emissao fiscal

  Cenario: Validar credenciais NFC-e por ambiente
    Quando o administrador salvar credenciais NFC-e com:
      | campo       | valor       |
      | ambiente    | HOMOLOGACAO |
      | cscId       | 1           |
      | cscToken    | CSC-TESTE   |
      | serie       | 1           |
      | proximoNumero | 100       |
    Entao a resposta fiscal deve ter status HTTP 201
    E as credenciais NFC-e devem ficar vinculadas ao ambiente "HOMOLOGACAO"
    E as credenciais NFC-e devem pertencer somente a filial ativa

  Cenario: Bloquear emissao real quando a filial nao estiver fiscalmente pronta
    Dado que a filial ativa nao possui dados fiscais completos
    E a filial ativa possui certificado A1 valido
    E existe uma venda finalizada e paga com itens fiscais validos
    Quando o usuario solicitar a emissao real da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "Filial nao esta pronta para emissao fiscal"
    E nenhum documento fiscal deve ser enviado para a SEFAZ

  Cenario: Cadastrar tributacao minima de produto para NFC-e
    Dado que existe um produto do tenant atual
    Quando o administrador salvar a tributacao fiscal do produto com:
      | campo        | valor    |
      | ncm          | 61091000 |
      | cfopVenda    | 5102     |
      | origem       | 0        |
      | csosn        | 102      |
      | cest         |          |
      | unidade      | UN       |
      | gtin         | SEM GTIN |
    Entao a resposta fiscal deve ter status HTTP 201
    E a tributacao fiscal deve pertencer ao tenant atual
    E a tributacao fiscal deve pertencer a filial ativa
    E a tributacao fiscal deve pertencer ao produto informado

  Cenario: Bloquear produto sem regra tributaria compativel com o regime da filial
    Dado que a filial ativa esta no regime "SIMPLES_NACIONAL"
    E existe um produto do tenant atual
    Quando o administrador tentar salvar tributacao do produto com CST do regime normal
    Entao a resposta fiscal deve ter status HTTP 400
    E a resposta fiscal deve informar "Tributacao do produto incompativel com o regime fiscal da filial"

  Cenario: Isolar prontidao fiscal entre tenants
    Dado que existe uma filial fiscalmente pronta em outro tenant
    Quando o administrador do tenant atual consultar a prontidao dessa filial
    Entao a resposta fiscal deve ter status HTTP 404

  Cenario: Isolar prontidao fiscal entre filiais do mesmo tenant
    Dado que existe uma filial fiscalmente pronta em outra filial do mesmo tenant
    Quando o administrador consultar a prontidao usando a filial ativa atual
    Entao a resposta fiscal deve ter status HTTP 404
