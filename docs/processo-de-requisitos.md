# Processo de engenharia de requisitos

Todo requisito novo do Kalles segue este ciclo, do levantamento à entrega. Correção de defeito não passa pelo ciclo completo, mas, se a correção mudar uma regra de negócio, a regra é registrada no requisito correspondente antes do código.

## Artefatos

| Artefato | Onde fica | Papel |
|---|---|---|
| Documento do requisito | `docs/requisitos/RF-NNN-<nome>.md` | História, regras de negócio, critérios de aceitação, decisões e histórico de versões |
| Especificação executável | `kalles-back/kalles-sale/src/test/resources/features/<módulo>/*.feature` | Um cenário Cucumber por critério de aceitação |
| Código | branch `feature/rf-NNN-<nome>` | Implementação em commits pequenos |
| Pull request | `feature/rf-NNN-<nome>` → `develop` | Revisão e checklist de pronto |

## Identificadores

- Requisito: `RF-001`, `RF-002`…
- Regra de negócio: `RN-001.1`, `RN-001.2`… (requisito.regra)
- Critério de aceitação: `CA-001.1`, `CA-001.2`…
- Decisão: `D-001.1`, `D-001.2`…
- A feature leva a tag `@RF-001`; cada cenário leva a tag do critério que verifica (`@CA-001.3`).
- O título do PR começa pelo identificador: `RF-001 Cadastro de cliente pessoa jurídica`.

Com isso é possível ir do requisito ao teste e ao código, e de volta.

## Etapas

### 1. Levantamento

Entender o problema antes da solução. Sai daqui o documento do requisito com:

- história de usuário (como / quero / para);
- situação atual, com base no código e no comportamento real do sistema;
- regras de negócio numeradas;
- critérios de aceitação em Dado / Quando / Então;
- fora de escopo;
- decisões em aberto.

### 2. Análise e modelagem

Traduzir o requisito para o sistema:

- modelo de domínio e impacto no banco (migration Flyway);
- contrato da API (endpoints, payloads, códigos de erro com `code`);
- esboço das telas afetadas;
- impacto em outros módulos.

As decisões tomadas aqui são registradas na seção **Decisões** do documento.

### 3. Especificação executável

Os critérios de aceitação viram cenários `.feature` **antes** da implementação. Os cenários falham até o código existir.

Critério de interface, que depende da tela, é verificado no teste manual de aceitação e fica sem cenário.

### 4. Implementação

Branch `feature/rf-NNN-<nome>` a partir de `develop`. Commits pequenos, uma unidade lógica por commit, mensagem em inglês no padrão conventional commits.

### 5. Verificação e aceitação

- todos os cenários do requisito passando;
- suíte completa verde;
- teste manual de aceitação pela interface, seguindo os critérios;
- pull request revisado contra o checklist de pronto.

### 6. Entrega e versionamento

Com o PR integrado, o requisito passa para **Entregue** na versão `1.0`. Uma mudança posterior abre uma nova versão do requisito (`1.1` para ajuste compatível, `2.0` para mudança de regra) e registra o motivo no histórico.

**Teste de requisito entregue só muda junto com uma nova versão do requisito.** Se um teste de aceitação quebra sem que o requisito tenha mudado, o defeito está no código, não no teste.

## Estados do requisito

`Rascunho` → `Aprovado` → `Em desenvolvimento` → `Entregue`

## Definição de pronto para começar (DoR)

Um requisito só sai de `Rascunho` para `Aprovado` quando:

- [ ] a história de usuário está escrita;
- [ ] as regras de negócio estão numeradas e sem ambiguidade;
- [ ] cada regra tem ao menos um critério de aceitação;
- [ ] não há decisão em aberto que bloqueie a modelagem;
- [ ] o que está fora de escopo está explícito.

## Definição de pronto para entregar (DoD)

Um requisito só passa para `Entregue` quando:

- [ ] cada critério de aceitação tem um cenário `.feature` passando, exceto os de interface;
- [ ] a suíte completa está verde;
- [ ] o teste manual de aceitação pela interface foi feito;
- [ ] README e documentação afetados foram atualizados;
- [ ] o PR foi integrado em `develop`;
- [ ] o documento do requisito foi atualizado (estado, versão e histórico).

## Modelo de documento de requisito

```markdown
# RF-NNN — <nome>

| Versão | Estado | Data |
|---|---|---|
| 1.0 | Rascunho | AAAA-MM-DD |

## História de usuário
Como <papel>, quero <capacidade>, para <benefício>.

## Situação atual

## Regras de negócio
- **RN-NNN.1** …

## Critérios de aceitação
- **CA-NNN.1** (RN-NNN.1) Dado … Quando … Então …

## Fora de escopo

## Decisões

## Decisões em aberto

## Histórico
| Versão | Data | Mudança |
|---|---|---|
```
