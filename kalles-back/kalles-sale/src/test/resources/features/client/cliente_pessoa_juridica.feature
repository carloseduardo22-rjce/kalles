# language: pt
@client @RF-001

Funcionalidade: Cadastro de cliente pessoa juridica
  Como administrador da loja
  Quero cadastrar clientes pessoa juridica com CNPJ
  Para identificar nas vendas os clientes que compram em nome de uma empresa

  Contexto:
    Dado um admin autenticado para clients

  @CA-001.1
  Cenario: Pessoa juridica com CNPJ numerico
    Quando ele cadastrar uma pessoa juridica "Mercado Bom Preco Ltda" com CNPJ "11.222.333/0001-81"
    Entao a resposta de client deve ter status HTTP 201
    E o cliente criado deve ter tipo "JURIDICA" e documento "11222333000181"

  @CA-001.2
  Cenario: CNPJ alfanumerico em minusculas e com pontuacao
    Quando ele cadastrar uma pessoa juridica "Padaria Aurora Ltda" com CNPJ "12.abc.345/01de-35"
    Entao a resposta de client deve ter status HTTP 201
    E o cliente criado deve ter tipo "JURIDICA" e documento "12ABC34501DE35"

  @CA-001.3
  Cenario: CNPJ com digito verificador invalido
    Quando ele cadastrar uma pessoa juridica "Padaria Aurora Ltda" com CNPJ "12ABC34501DE36"
    Entao a resposta de client deve ter status HTTP 400
    E o detalhe do erro de client deve conter "document: CNPJ inválido"

  @CA-001.4
  Cenario: CNPJ repetido na mesma filial
    Dado uma pessoa juridica cadastrada na filial ativa com CNPJ "11222333000181"
    Quando ele cadastrar uma pessoa juridica "Outra Empresa Ltda" com CNPJ "11.222.333/0001-81"
    Entao a resposta de client deve ter status HTTP 409
    E o codigo de erro de client deve ser "CLIENT_DOCUMENT_ALREADY_EXISTS"

  @CA-001.5
  Cenario: Mesmo CNPJ em outra filial do mesmo tenant
    Dado uma pessoa juridica cadastrada em outra filial com CNPJ "11222333000181"
    Quando ele cadastrar uma pessoa juridica "Mercado Bom Preco Ltda" com CNPJ "11222333000181"
    Entao a resposta de client deve ter status HTTP 201

  @CA-001.6
  Esquema do Cenario: Campo exclusivo de pessoa fisica em pessoa juridica
    Quando ele cadastrar uma pessoa juridica com CNPJ "11222333000181" informando "<campo>" igual a "<valor>"
    Entao a resposta de client deve ter status HTTP 400
    E o detalhe do erro de client deve conter "<campo>:"

    Exemplos:
      | campo      | valor       |
      | birthDate  | 1990-05-20  |
      | gender     | M           |
      | rg         | 1234567     |
      | nameFather | Jose Souza  |
      | nameMother | Maria Souza |

  @CA-001.7
  Cenario: Pessoa fisica continua como antes
    Quando ele cadastrar uma pessoa fisica "Maria Souza" com CPF "529.982.247-25"
    Entao a resposta de client deve ter status HTTP 201
    E o cliente criado deve ter tipo "FISICA" e documento "52998224725"

  @CA-001.8
  Cenario: Tipo do cliente nao muda depois do cadastro
    Dado uma pessoa fisica cadastrada na filial ativa com CPF "52998224725"
    Quando ele alterar esse cliente para pessoa juridica com CNPJ "11222333000181"
    Entao a resposta de client deve ter status HTTP 422
    E o codigo de erro de client deve ser "CLIENT_PERSON_TYPE_IMMUTABLE"

  @CA-001.9
  Cenario: Cliente gravado antes do requisito aparece como pessoa fisica
    Dado um cliente gravado sem tipo na filial ativa com documento "52998224725"
    Quando ele consultar esse cliente
    Entao a resposta de client deve ter status HTTP 200
    E o cliente consultado deve ter tipo "FISICA"

  @CA-001.11
  Cenario: Pessoa juridica fora do programa de fidelidade
    Dado uma politica de fidelidade ativa na filial ativa
    E uma pessoa juridica cadastrada na filial ativa com CNPJ "11222333000181"
    Quando ele inscrever esse cliente no programa de fidelidade
    Entao a resposta de client deve ter status HTTP 422
    E o codigo de erro de client deve ser "LEGAL_ENTITY_NOT_ELIGIBLE_FOR_FIDELITY"
