# Documentação Técnica de Alto Nível — Projeto Kalles

## 1. Visão Geral do Sistema

O **Kalles** é um ERP focado em **pequenas lojas e mercados**, com o **PDV (Ponto de Venda)** como módulo central da operação diária.

Na prática, o sistema cobre o ciclo completo de venda:

- cadastro de produtos e estoque
- abertura e fechamento de caixa
- registro e processamento de vendas
- gestão de clientes, operadores e empresas
- relatórios operacionais e financeiros
- integrações de pagamento

### Objetivo do produto

Permitir que o lojista opere o dia a dia com rapidez no caixa e controle básico de gestão, sem precisar de uma estrutura complexa de TI.

### Perfil de uso

- negócios com operação presencial
- equipes pequenas (caixa, gerente, operador)
- necessidade de múltiplas empresas/filiais no mesmo ambiente de software

---

## 2. Arquitetura do Sistema

## 2.1 Visão macro

O sistema está dividido em duas aplicações principais:

- **Backend** em Spring Boot (módulo `kalles-sale`)
- **Frontend** em Next.js (App Router)

Persistência em **PostgreSQL** e versionamento de esquema com **Flyway**.

## 2.2 Backend (Spring Boot + arquitetura hexagonal)

A arquitetura segue o modelo **Ports and Adapters (hexagonal)**, principalmente nos módulos de integração (ex.: Mercado Pago e Billing).

### Camadas e responsabilidades

- **Domínio (`domain`)**: entidades e regras centrais.
- **Aplicação (`application`)**: casos de uso e orquestração.
- **Portas (`port`)**: contratos de entrada/saída.
- **Adaptadores (`adapter`)**: implementação de API externa, persistência e web.
- **API/Controllers (`api` / `adapter.in.web`)**: exposição HTTP.

Esse formato facilita a troca de infraestrutura sem reescrever regras de negócio.

## 2.3 Frontend (Next.js)

O frontend está organizado por **features** e rotas de aplicação (PDV, produtos, relatórios, suporte etc.), com cliente HTTP compartilhado e contexto da empresa ativa.

Pontos importantes:

- API client centraliza chamadas com `credentials: include`.
- Cabeçalho `X-Company-ID` é enviado para escopo de empresa ativa.
- Páginas principais incluem login, registro, PDV e módulos administrativos.

## 2.4 Persistência

- Banco principal: **PostgreSQL**
- Migrations: **Flyway**
- `ddl-auto: none` para evitar drift de schema em produção

## 2.5 Segurança e contexto de execução

- Autenticação baseada em segurança Spring + JWT
- Contextos por tenant/empresa/dispositivo de PDV via holders de contexto
- Controle de escopo por headers e sessão

---

## 3. Domínios e Módulos

## 3.1 Core de vendas (domínio principal)

Responsável pelo fluxo de venda no PDV:

- sessão de venda
- itens e pagamentos
- estados da venda (aberta, em pagamento, concluída, cancelada etc.)
- regras de autorização para operações sensíveis (ex.: cancelamento)

Também concentra entidades de apoio como produto, estoque, operador, cliente, empresa, metas e fidelidade.

## 3.2 Caixa (cash register)

Gerencia:

- cadastro do caixa físico/lógico
- abertura de sessão com valor inicial
- fechamento de sessão
- consistência do status da sessão para permitir venda

## 3.3 Segurança e identidade

Módulo com:

- autenticação e registro de contas
- verificação de conta
- sessão de dispositivo PDV
- carregamento de identidade e escopo por tenant/empresa

## 3.4 Mercado Pago

Módulo dedicado para operação com Mercado Pago:

- vinculação OAuth por tenant
- criação/listagem de loja e POS
- ativação de modo PDV em terminais
- criação/consulta/cancelamento/estorno de cobrança
- processamento de webhook

A estrutura deste módulo é fortemente hexagonal (ports/use cases/adapters).

## 3.5 Billing (assinatura/financeiro da plataforma)

Módulo de billing com gateway Stripe:

- checkout de assinatura
- portal de cobrança
- processamento de webhook de billing
- persistência de eventos e assinatura

## 3.6 Suporte e notas sensíveis

Há módulos de suporte e notas com tratamento de conteúdo sensível por tenant, usados para atendimento interno e organização de informações.

---

## 4. Fluxos principais do sistema

## 4.1 Fluxo de autenticação e contexto

1. Usuário realiza login.
2. Backend valida credenciais e estabelece contexto de acesso.
3. Frontend carrega empresas disponíveis do usuário.
4. Usuário seleciona empresa ativa.
5. Frontend passa `X-Company-ID` nas chamadas seguintes.

**Resultado:** todas as telas e operações passam a operar no escopo correto da empresa.

## 4.2 Fluxo de venda no PDV (happy path)

1. Operador abre sessão de caixa.
2. Inicia venda e adiciona itens.
3. Venda entra em pagamento.
4. Pagamentos são registrados até quitar `amountDue`.
5. Venda é concluída.

Estados de venda típicos:

`OPEN -> PAYMENT_IN_PROGRESS -> PAID -> COMPLETED`

## 4.3 Fluxo de exceção de venda

- **Pausar venda:** OPEN -> ON_HOLD
- **Retomar venda:** ON_HOLD -> OPEN
- **Cancelar venda:** permitido com validação de permissão/autorização

Esse controle por estado evita operações inválidas durante o atendimento no caixa.

## 4.4 Fluxo de integração Mercado Pago

1. Tenant vincula conta Mercado Pago (OAuth).
2. Sistema configura loja/POS/terminal.
3. PDV cria cobrança (Point/QR, conforme cenário).
4. Sistema consulta status da cobrança e/ou recebe webhook.
5. Pagamento confirmado atualiza estado da ordem e da venda.

## 4.5 Fluxo de billing da plataforma

1. Tenant inicia checkout de assinatura.
2. Provedor processa pagamento.
3. Webhook confirma evento de cobrança.
4. Backend atualiza status da assinatura local.

---

## 5. Regras de negócio importantes

## 5.1 Multi-tenant como regra transversal

- Dados e integrações são isolados por tenant.
- Seleção de empresa no frontend afeta todas as operações.
- Credenciais externas (ex.: Mercado Pago) são associadas por tenant.

## 5.2 Máquina de estados da venda

A venda só pode receber ações válidas para o estado atual.

Exemplo:

- Em `OPEN`: pode adicionar item, iniciar pagamento, pausar, cancelar.
- Em `PAID`: deve seguir para conclusão.
- Em estado terminal (`COMPLETED`/`CANCELED`): não aceita novas mutações.

## 5.3 Controle de permissão operacional

Operações sensíveis (remoção de item, cancelamento) exigem:

- perfil com permissão suficiente, ou
- autorização de operador com nível superior

Esse ponto reduz risco de fraude e erro operacional no caixa.

## 5.4 Sessão de caixa obrigatória

O fluxo de venda depende de sessão de caixa válida/aberta. Sem sessão ativa, o fluxo operacional é bloqueado.

## 5.5 Integridade de pagamento

Conclusão da venda depende de quitação financeira (`amountDue`), evitando fechamento inconsistente.

---

## 6. Integrações externas

## 6.1 Mercado Pago

**Uso principal:** pagamentos de PDV e operação com terminais.

Capacidades atuais no backend:

- OAuth e vínculo por tenant
- cadastro/listagem de recursos POS
- geração e gestão de cobranças
- webhook para atualização assíncrona de status

## 6.2 Stripe (Billing)

**Uso principal:** cobrança da assinatura da plataforma (ERP SaaS).

Capacidades atuais:

- criação de sessão de checkout
- portal de cobrança
- recebimento e processamento de eventos via webhook

## 6.3 E-mail transacional

Há suporte a envio de e-mail (SMTP) para fluxos de conta/comunicação.

## 6.4 Stone

No contexto de produto, a Stone é uma integração desejada. Na base atual, o módulo explícito já estruturado e ativo é o de Mercado Pago; recomenda-se tratar Stone como **roadmap** seguindo o mesmo padrão de portas e adaptadores para manter consistência arquitetural.

---

## 7. Guia para novos desenvolvedores

## 7.1 Como pensar o projeto

Antes de codar, entenda três eixos:

1. **Domínio PDV** (estado da venda, caixa, permissões)
2. **Escopo multi-tenant** (tenant + empresa ativa)
3. **Integrações externas** (Mercado Pago/Billing via portas e adaptadores)

Se um requisito tocar nesses três pontos, ele é crítico e deve ser modelado com cuidado.

## 7.2 Onboarding técnico sugerido (primeira semana)

### Dia 1 — Setup e visão geral

- subir backend com PostgreSQL
- subir frontend
- validar login e navegação básica

### Dia 2 — Fluxo PDV ponta a ponta

- abrir sessão de caixa
- criar venda, adicionar itens, pagar e concluir
- testar pausa/cancelamento para entender estados

### Dia 3 — Multi-tenant

- alternar empresa ativa no frontend
- validar impacto no backend via headers/contexto

### Dia 4 — Integrações

- mapear pontos de entrada/saída do Mercado Pago
- mapear webhook e persistência de status

### Dia 5 — Primeiro incremento

- escolher tarefa pequena de domínio
- validar regras existentes antes de alterar comportamento
- manter separação entre domínio, aplicação e infraestrutura

## 7.3 Boas práticas para contribuir

- priorize alterações orientadas a caso de uso
- evite acoplamento direto do domínio com SDK externo
- preserve contratos de porta para facilitar troca de provedores
- mantenha atenção ao escopo de tenant/empresa em toda endpoint
- trate estados de venda como regra central, não detalhe de UI

## 7.4 Checklist antes de abrir PR

- o fluxo PDV principal continua íntegro?
- alguma regra de permissão foi afetada?
- o isolamento multi-tenant foi preservado?
- integrações externas seguem via adaptadores?
- houve impacto de schema/migration?

---

## Resumo executivo

O Kalles combina um **núcleo forte de PDV** com **arquitetura escalável para integrações**. O desenho atual privilegia:

- operação de caixa segura e rastreável
- evolução de integrações sem quebrar domínio
- isolamento multi-tenant para uso real em pequenas redes

Para novos desenvolvedores, o caminho mais rápido é dominar primeiro o fluxo de venda, depois o contexto multi-tenant e, por fim, os módulos de integração.
