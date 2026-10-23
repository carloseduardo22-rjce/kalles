# RF-001 — Cadastro de cliente pessoa jurídica

| Versão | Estado | Data |
|---|---|---|
| 1.0 | Entregue | 2026-10-23 |

## História de usuário

Como administrador da loja, quero cadastrar clientes pessoa jurídica (empresas) com CNPJ, para atender e identificar nas vendas os clientes que compram em nome de uma empresa.

## Situação atual

- O cliente (`Client`) só existe como pessoa física: CPF obrigatório e validado (`@CPF`), único por loja (`uk_client_cpf_company`).
- Os demais campos são de pessoa física: data de nascimento, gênero, RG, nome do pai e nome da mãe.
- O CPF do cliente não é usado por outros módulos do back-end. No front-end, o painel de fidelidade do PDV localiza o cliente pelo CPF.
- A venda e a fidelidade referenciam o cliente pelo identificador interno, não pelo documento.

## Regras de negócio

- **RN-001.1** Todo cliente tem um tipo: pessoa física (`FISICA`) ou pessoa jurídica (`JURIDICA`). Clientes já cadastrados são pessoa física.
- **RN-001.2** O documento do cliente é obrigatório: CPF para pessoa física, CNPJ para pessoa jurídica.
- **RN-001.3** O CNPJ é validado pelos dígitos verificadores e aceita os dois formatos vigentes: numérico e alfanumérico (CNPJ alfanumérico emitido pela Receita Federal a partir de julho de 2026). Letras são armazenadas em maiúsculas; pontuação é descartada.
- **RN-001.4** O documento é único por loja, independentemente do tipo: não pode haver dois clientes da mesma loja com o mesmo CPF ou o mesmo CNPJ.
- **RN-001.5** Pessoa jurídica tem razão social (obrigatória, 3 a 100 caracteres), nome fantasia (opcional) e inscrição estadual (opcional; aceita o valor `ISENTO`).
- **RN-001.6** Campos exclusivos de pessoa física (data de nascimento, gênero, RG, nome do pai, nome da mãe) não são aceitos para pessoa jurídica.
- **RN-001.7** O tipo do cliente não muda depois do cadastro. Para trocar, o cliente é excluído e cadastrado de novo.
- **RN-001.8** Celular, código do país e observações valem para os dois tipos, com as mesmas regras atuais.
- **RN-001.9** Cliente pessoa jurídica não participa do programa de fidelidade.
- **RN-001.10** Na listagem de clientes, a pessoa jurídica aparece pelo nome fantasia, com a razão social abaixo; sem nome fantasia, aparece pela razão social.

## Critérios de aceitação

- **CA-001.1** (RN-001.2, RN-001.3, RN-001.5) Dado um administrador autenticado com loja ativa, quando ele cadastra um cliente pessoa jurídica com razão social e CNPJ numérico válido, então o cliente é criado com tipo `JURIDICA` e o CNPJ armazenado só com os 14 caracteres.
- **CA-001.2** (RN-001.3) Dado um administrador autenticado, quando ele cadastra um cliente pessoa jurídica com CNPJ alfanumérico válido informado em minúsculas e com pontuação, então o cliente é criado e o CNPJ é armazenado em maiúsculas, sem pontuação.
- **CA-001.3** (RN-001.3) Dado um administrador autenticado, quando ele informa um CNPJ com dígito verificador inválido, então o cadastro é recusado com status 400 e mensagem indicando CNPJ inválido.
- **CA-001.4** (RN-001.4) Dado um cliente pessoa jurídica cadastrado na loja, quando o administrador cadastra outro cliente com o mesmo CNPJ na mesma loja, então o cadastro é recusado com status 409 e `code` próprio de documento duplicado.
- **CA-001.5** (RN-001.4) Dado um cliente pessoa jurídica cadastrado na loja A, quando o administrador cadastra o mesmo CNPJ na loja B do mesmo tenant, então o cadastro é aceito.
- **CA-001.6** (RN-001.6) Dado um administrador autenticado, quando ele cadastra um cliente pessoa jurídica informando data de nascimento ou RG, então o cadastro é recusado com status 400.
- **CA-001.7** (RN-001.1, RN-001.2) Dado um administrador autenticado, quando ele cadastra um cliente pessoa física com CPF válido, então o comportamento é o mesmo de antes deste requisito.
- **CA-001.8** (RN-001.7) Dado um cliente pessoa física cadastrado, quando o administrador tenta alterá-lo para pessoa jurídica, então a alteração é recusada com status 422.
- **CA-001.9** (RN-001.1) Dado um cliente cadastrado antes deste requisito, quando ele é consultado, então aparece como pessoa física.
- **CA-001.10** Dado um administrador na tela de clientes, quando ele escolhe "Pessoa jurídica" no formulário, então o formulário mostra razão social, nome fantasia, CNPJ e inscrição estadual, e esconde os campos exclusivos de pessoa física.
- **CA-001.11** (RN-001.9) Dado um cliente pessoa jurídica cadastrado, quando se tenta inscrevê-lo no programa de fidelidade, então a inscrição é recusada com status 422 e `code` próprio.
- **CA-001.12** (RN-001.10) Dado um cliente pessoa jurídica com nome fantasia, quando o administrador abre a lista de clientes, então o cliente aparece pelo nome fantasia, com a razão social abaixo.

## Fora de escopo

- Endereço do cliente (nenhum tipo de cliente tem endereço hoje).
- Identificação do cliente pessoa jurídica na NFC-e.
- Consulta automática de dados do CNPJ em serviço externo.
- Benefícios comerciais para cliente pessoa jurídica (tabela de preço, desconto negociado, prazo). Se houver necessidade, vira requisito próprio.

## Decisões

- **D-001.1** (2026-10-16) Uma única entidade `Client` com `personType` e `document`. Venda e fidelidade continuam referenciando o mesmo cliente; os clientes existentes migram como pessoa física com o CPF atual em `document`.
- **D-001.2** (2026-10-16) Pessoa jurídica fica fora do programa de fidelidade nesta versão. Fidelidade por pontos é mecanismo de retenção do consumidor final; para empresa, quem compra é um funcionário, e pontos acumulados pela empresa abririam espaço para resgate indevido.
- **D-001.3** (2026-10-16) Listagem mostra o nome fantasia, com a razão social abaixo.
- **D-001.4** (2026-10-20) O campo `name` guarda o nome da pessoa física ou a razão social da pessoa jurídica. Venda, fidelidade e listagem continuam lendo `name`; o nome fantasia fica num campo próprio.
- **D-001.5** (2026-10-20) Banco: a coluna `cpf` passa a se chamar `document`, sem pontuação e obrigatória; entram `person_type` (padrão `FISICA`, restrito a `FISICA` e `JURIDICA`), `trade_name` e `state_registration`; a unicidade passa a ser `uk_client_document_company (document, company_id)`.
- **D-001.6** (2026-10-20) Contrato de `/api/clients`, nos mesmos endpoints:

  | Campo | Pessoa física | Pessoa jurídica |
  |---|---|---|
  | `personType` | `FISICA` (obrigatório) | `JURIDICA` (obrigatório) |
  | `document` | CPF (obrigatório) | CNPJ numérico ou alfanumérico (obrigatório) |
  | `name` | nome, 3 a 100 caracteres | razão social, 3 a 100 caracteres |
  | `tradeName` | não aceito | opcional, até 100 caracteres |
  | `stateRegistration` | não aceito | opcional, dígitos ou `ISENTO` |
  | `birthDate`, `gender`, `rg`, `nameFather`, `nameMother` | como antes | não aceitos |
  | `codeCountry`, `cellphone`, `observations` | como antes | como antes |

  A resposta troca `cpf` por `document` e inclui `personType`, `tradeName` e `stateRegistration`. Códigos de erro: `CLIENT_DOCUMENT_ALREADY_EXISTS` (409, substitui `CLIENT_CPF_ALREADY_EXISTS`), `CLIENT_PERSON_TYPE_IMMUTABLE` (422) e `LEGAL_ENTITY_NOT_ELIGIBLE_FOR_FIDELITY` (422).
- **D-001.7** (2026-10-20) A validação por tipo usa grupos do Bean Validation escolhidos pelo `personType` (`@GroupSequenceProvider`), com `@CPF` para pessoa física e `@CNPJ(format = ALPHANUMERIC)` do Hibernate Validator para pessoa jurídica, que já calcula o dígito verificador do CNPJ alfanumérico pela regra da Receita Federal. O documento é normalizado para letras maiúsculas e sem pontuação antes da validação.
- **D-001.8** (2026-10-20) Os critérios de interface (CA-001.10 e CA-001.12) são verificados no teste manual de aceitação; os demais têm cenário Cucumber.

## Histórico

| Versão | Data | Mudança |
|---|---|---|
| 1.0 | 2026-10-16 | Levantamento concluído e aprovado |
| 1.0 | 2026-10-20 | Análise e modelagem registradas; requisito em desenvolvimento |
| 1.0 | 2026-10-23 | Entregue: PR #9 integrado em develop, aceitação manual aprovada |
