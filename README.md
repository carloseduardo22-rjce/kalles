# Kalles

Sistema de gestão para lojas e mercados — PDV (frente de caixa), controle de estoque multi-loja, integração com maquininhas de cartão, emissão fiscal e programa de fidelidade.

Construído como plataforma **multi-tenant**: uma instalação atende várias empresas, cada uma com suas filiais, operadores, produtos e caixas isolados.

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen)
![Next.js](https://img.shields.io/badge/Next.js-16-black)
![React](https://img.shields.io/badge/React-19-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)
![License](https://img.shields.io/badge/license-MIT-green)

> **Status:** projeto em desenvolvimento ativo, usado como laboratório de arquitetura e boas práticas. Não está em produção.

---

## Índice

- [O que o sistema faz](#o-que-o-sistema-faz)
- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Modelo multi-tenant](#modelo-multi-tenant)
- [Rodando localmente](#rodando-localmente)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Testes](#testes)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Roadmap](#roadmap)
- [Licença](#licença)

---

## O que o sistema faz

### Frente de caixa (PDV)
Operação de venda completa: abertura e fechamento de sessão de caixa, leitura por código de barras ou código interno, descontos por item com autorização de supervisor, cancelamento auditado e finalização com múltiplas formas de pagamento.

Toda operação sensível (remoção de item, desconto, cancelamento) gera um **evento de auditoria** com o operador que executou e o supervisor que autorizou.

### Pagamentos
Integração com **Mercado Pago** e **Stone** para pagamento via terminal físico. O fluxo é assíncrono: o sistema envia a intenção de pagamento ao terminal e confirma a venda quando o webhook do provedor chega. Credenciais de cada tenant ficam cifradas em banco.

### Estoque multi-depósito
Produtos com catálogo global e preço por empresa, distribuídos em depósitos e localizações. Baixa automática na conclusão da venda, com validação de disponibilidade na adição do item e revalidação no momento da baixa.

### Fiscal
Emissão de documento fiscal eletrônico com ambiente de homologação e produção separados.

### Fidelidade
Acúmulo de pontos por venda e conversão em desconto, com política configurável por empresa.

### Gestão e relatórios
Cadastro de produtos, clientes, operadores, caixas, depósitos e metas. Relatórios de vendas com exportação para Excel. Painel de assinatura via **Stripe**.

### Suporte
Sistema de tickets interno com histórico por empresa.

---

## Stack

**Backend**
- Java 25, Spring Boot 4.0.1
- Spring Web MVC, Spring Data JPA, Spring Security
- PostgreSQL 17 + Flyway (48 migrations versionadas + seed repetível)
- JWT (`java-jwt`) em cookie `HttpOnly` + refresh token
- Thymeleaf (templates de e-mail)
- SpringDoc OpenAPI

**Frontend**
- Next.js 16 (App Router), React 19, TypeScript
- Tailwind CSS 4 + shadcn/ui (Radix)
- TanStack Query
- React Hook Form + Zod
- Recharts

**Integrações**
- Mercado Pago (pagamento presencial + Point)
- Stone (pagamento presencial)
- Stripe (cobrança de assinatura do SaaS)
- NF-e

**Testes**
- JUnit 5, Cucumber (BDD, 26 features), REST Assured
- Testcontainers (PostgreSQL) — em adoção
- Cypress (e2e)

---

## Arquitetura

Monólito modular. O backend é um projeto Maven multi-módulo com um módulo de aplicação (`kalles-sale`), organizado internamente **por feature** — cada domínio é um pacote autocontido, não uma fatia de camada técnica global.

```
kalles-back/kalles-sale/src/main/java/dev/kalles/
│
│   # Features de negócio — MVC por feature
├── sale/           # Venda: itens, estados, pagamento, histórico
├── cashregister/   # Caixa, sessão, operador, permissões
├── inventory/      # Estoque, entrada, depósito, localização
├── product/        # Produto e produto por empresa
├── client/         # Cliente
├── fidelity/       # Programa de fidelidade
├── goal/           # Metas
├── company/        # Empresa e tenant
├── report/         # Relatórios financeiros
├── support/        # Chamados e atendimento
│
│   # Integrações — arquitetura hexagonal
├── payment/        # Pagamento: portas e adaptadores (Mercado Pago, Stone)
├── fiscal/         # Emissão de documento fiscal
├── billing/        # Assinatura SaaS via Stripe
├── note/           # Notas e conteúdo sensível
├── email/          # Envio transacional
│
│   # Transversais
├── security/       # JWT, contexto de tenant, autorização
└── shared/         # Base de entidade auditável, exceções, DTOs comuns
                    # e o tratamento global de erros (RFC 7807)
```

Dentro de cada feature o layout é o mesmo: `controller/ dto/ entity/ enums/ repository/ service/`, com pastas extras quando o domínio pede — `sale/` tem `state/` e `strategy/`, `inventory/` tem `exception/`.

**Dois estilos convivem de propósito.** As integrações seguem **arquitetura hexagonal** — o domínio define portas, os adaptadores implementam contra SDKs externos, o que mantém a regra de negócio testável sem rede. As features de negócio seguem **MVC por feature**, que é mais direto para CRUD transacional e não paga o custo de indireção de portas onde não há fornecedor externo a trocar.

Os erros da API seguem **RFC 7807 (`application/problem+json`)** via `ProblemDetail`.

`support/` segue o mesmo layout, com uma pasta a mais: `domain/` guarda o modelo rico de chamado (`Ticket` e seus estados), separado das entidades JPA por um mapper. É uma separação deliberada, não um resquício.

### Frontend

```
frontend/
├── app/            # Rotas (App Router) — (app) autenticado, pdv, login
├── features/       # Módulos por domínio: sales, payment, reports, admin
├── shared/         # Serviços de API, contexts, store, tipos
├── components/     # Componentes compartilhados + ui/ (shadcn)
└── hooks/
```

---

## Modelo multi-tenant

Três níveis de contexto, resolvidos no `JwtAuthenticationFilter` a cada requisição:

| Nível | Origem | Significado |
|-------|--------|-------------|
| **Tenant** | claim `tenantId` do JWT | A conta contratante (o cliente do SaaS) |
| **Company** | claim `companyId` ou header `X-Company-ID` | A filial ativa da operação |
| **POS** | claim `posId` do JWT | O terminal de PDV autenticado |

O contexto é propagado por request e todas as queries de domínio filtram por `tenantId` / `companyId`. Quando o header `X-Company-ID` é enviado, o filtro valida que a filial pertence ao tenant autenticado antes de aceitá-la — uma filial de outro tenant resulta em `403`.

Rotas administrativas e de PDV exigem contexto de filial explícito; a ausência retorna `400 COMPANY_CONTEXT_REQUIRED`.

---

## Rodando localmente

### Pré-requisitos

- JDK 25
- Node.js 20+ e npm
- Docker (para o PostgreSQL)

### 1. Banco de dados

```bash
cd kalles-back
docker compose up -d
```

Sobe PostgreSQL em `localhost:5432` e pgAdmin em `localhost:8081`.

> O volume do Postgres usa `tmpfs` — **os dados são descartados ao parar o container**. É intencional para desenvolvimento: cada subida roda as migrations do zero. Para persistir, troque por um volume nomeado no `docker-compose.yml`.

### 2. Backend

A aplicação sobe apenas com `TENANT_CREDENTIALS_SECRET` definida — as demais variáveis têm default e desabilitam a integração correspondente quando ausentes (veja [variáveis de ambiente](#variáveis-de-ambiente)):

```bash
cd kalles-back
TENANT_CREDENTIALS_SECRET=dev-secret ./mvnw spring-boot:run -pl kalles-sale
```

Como alternativa às variáveis, crie `kalles-sale/src/main/resources/application-local.yml` (ignorado pelo git) e ative o profile:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run -pl kalles-sale
```

> O profile `local` **não** é ativado automaticamente. Ele foi retirado do `application.yml` versionado justamente porque apontava para um arquivo ignorado pelo git: quem clonasse o repositório não conseguia subir a aplicação, e a CI quebrava em todo build.

API em `http://localhost:8080`. Swagger em `http://localhost:8080/swagger-ui.html`.

O Flyway aplica as migrations automaticamente na subida.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Aplicação em `http://localhost:3000`. As chamadas a `/api/*` são reescritas para o backend em `:8080` (veja `next.config.mjs`).

---

## Variáveis de ambiente

Nenhum segredo é versionado. O backend lê de variáveis de ambiente ou de `application-local.yml` (ignorado pelo git).

| Variável | Obrigatória | Descrição |
|----------|-------------|-----------|
| `TENANT_CREDENTIALS_SECRET` | ✅ | Chave de cifragem das credenciais de gateway por tenant. **Única sem default** — sem ela a aplicação não sobe |
| `MAIL_USERNAME` | | Conta SMTP para e-mails transacionais |
| `MAIL_PASSWORD` | | Senha de app da conta SMTP |
| `APP_SECURE_COOKIES` | | `true` em produção (exige HTTPS). Padrão: `false` |
| `STRIPE_SECRET_KEY` | | Cobrança de assinatura |
| `STRIPE_PUBLISHABLE_KEY` | | Chave pública Stripe (frontend) |
| `STRIPE_WEBHOOK_SECRET` | | Validação de webhook Stripe |
| `STRIPE_MONTHLY_PRICE_ID` | | ID do plano mensal |
| `MERCADOPAGO_APP_ID` | | Aplicação Mercado Pago (OAuth) |
| `MERCADOPAGO_ACCESS_TOKEN` | | Token de acesso Mercado Pago (fallback da aplicação) |
| `MERCADOPAGO_CLIENT_ID` | | Client ID do OAuth Mercado Pago |
| `MERCADOPAGO_CLIENT_SECRET` | | Client secret do OAuth Mercado Pago |
| `MERCADOPAGO_REDIRECT_URI` | | Callback do OAuth Mercado Pago |
| `MERCADOPAGO_WEBHOOK_SECRET` | | Validação de webhook Mercado Pago |
| `STONE_WEBHOOK_SECRET` | | Validação de webhook Stone |
| `PAYMENTS_SIMULATION_ENABLED` | | `true` permite pagamento simulado sem terminal. Padrão: `false` |

As integrações são opcionais: sem as chaves, o sistema sobe e os módulos correspondentes ficam indisponíveis.

---

## Testes

```bash
# Backend
cd kalles-back
./mvnw test

# Frontend (e2e — exige back e front rodando)
cd frontend
npm run e2e
```

O backend combina testes unitários, testes de API com REST Assured e **26 features Cucumber** cobrindo fluxo de PDV, sessão de caixa, isolamento multi-tenant, emissão fiscal, pagamento via Stone/Mercado Pago, fidelidade, metas e assinatura.

> **Limitação conhecida:** a suíte roda majoritariamente sobre H2 com schema gerado pelo Hibernate e Flyway desabilitado. As migrations não são exercitadas pelos testes, e comportamentos específicos do PostgreSQL (índices únicos parciais, por exemplo) não são cobertos. Migrar para Testcontainers está no [roadmap](#roadmap).

---

## Estrutura do repositório

```
.
├── frontend/         # Next.js 16
├── kalles-back/
│   ├── kalles-sale/  # Módulo de aplicação Spring Boot
│   ├── docs/         # Documentação técnica e diagramas
│   └── docker-compose.yml
└── LICENSE
```

---

## Roadmap

Melhorias identificadas em code review, em ordem de prioridade:

- [ ] Lock pessimista na baixa de estoque para eliminar risco de oversell concorrente
- [ ] Decompor `SaleService` em use cases por operação
- [ ] Substituir `ThreadLocal` por `ScopedValue` (Java 25) nos context holders
- [ ] Testcontainers como padrão nos testes de integração, no lugar do H2
- [ ] Hierarquia de exceções de domínio no lugar de `IllegalStateException`/`IllegalArgumentException`
- [ ] Reativar checagem de tipos no build do frontend e configurar ESLint
- [ ] Testes unitários de frontend (Vitest + Testing Library)
- [ ] Quebrar páginas do admin em componentes menores

---

## Licença

MIT — veja [LICENSE](LICENSE).
