# RF-003 — Autorização de supervisor no caixa

| Versão | Estado | Data |
|---|---|---|
| 1.0 | Rascunho | 2026-10-16 |

## História de usuário

Como gerente da loja, quero que as operações sensíveis do caixa só aconteçam com permissão de quem pode autorizá-las, comprovada no momento e registrada, para controlar perdas e saber quem liberou cada operação.

## Situação atual

- Quatro operações dependem de autorização: remover item da venda, cancelar venda, aplicar desconto manual em item e fechar o caixa.
- A permissão é uma escada fixa de níveis do cadastro de operador (`BASIC` < `SUPERVISOR` < `MANAGER` < `ADMIN`). Remover item, cancelar venda e dar desconto: nível `SUPERVISOR` ou acima executa direto; abaixo disso, precisa de alguém de nível maior. Fechar caixa: precisa de um autorizador `SUPERVISOR` ou acima.
- A autorização é feita informando só o código do operador autorizador (por exemplo `OP-002`), sem senha ou PIN. Quem souber o código de um supervisor autoriza qualquer operação.
- As autorizações não ficam registradas de forma consultável.

## Regras de negócio

- **RN-003.1** As operações sensíveis do caixa formam uma lista fechada: remover item, cancelar venda, desconto manual em item e fechar caixa com diferença acima da tolerância (RF-002).
- **RN-003.2** Cada perfil de acesso define, para cada operação sensível, se quem tem o perfil pode **executar sem liberação** e se pode **autorizar** a operação de outra pessoa. As duas permissões são independentes.
- **RN-003.3** A loja configura as permissões dos perfis. Uma loja nova começa com uma configuração padrão: operador não executa nem autoriza; supervisor executa e autoriza; administrador executa e autoriza tudo.
- **RN-003.4** Quando a pessoa no caixa não pode executar a operação sem liberação, o caixa pede a liberação de alguém que possa autorizá-la.
- **RN-003.5** A liberação é presencial, no próprio caixa: o autorizador informa a sua identificação e o seu PIN pessoal.
- **RN-003.6** O PIN é pessoal, numérico, de 4 a 6 dígitos, cadastrado pela própria pessoa e armazenado só como hash. Ninguém consegue ver o PIN de outra pessoa.
- **RN-003.7** Depois de 5 PINs errados seguidos, a liberação por aquela pessoa fica bloqueada por 15 minutos.
- **RN-003.8** Uma pessoa não autoriza a própria operação.
- **RN-003.9** Cada liberação vale para uma única operação.
- **RN-003.10** Toda tentativa de liberação, aceita ou recusada, é registrada com: operação, quem pediu, quem autorizou (ou tentou), caixa, sessão, venda e item quando houver, valor envolvido, data e hora e resultado.
- **RN-003.11** O administrador consulta o registro de liberações filtrando por período, caixa, pessoa e operação.

## Critérios de aceitação

- **CA-003.1** (RN-003.2, RN-003.4) Dado um operador sem permissão de cancelar venda, quando ele cancela uma venda, então o caixa pede liberação de supervisor antes de cancelar.
- **CA-003.2** (RN-003.2) Dado um perfil com permissão de executar cancelamento, quando uma pessoa desse perfil cancela uma venda, então o cancelamento acontece sem liberação.
- **CA-003.3** (RN-003.5) Dado um pedido de liberação, quando o supervisor informa identificação e PIN corretos e tem permissão de autorizar a operação, então a operação é executada.
- **CA-003.4** (RN-003.5) Dado um pedido de liberação, quando o PIN informado está errado, então a operação não é executada e a tentativa é registrada como recusada.
- **CA-003.5** (RN-003.2) Dado um pedido de liberação, quando quem informa o PIN não tem permissão de autorizar aquela operação, então a operação não é executada.
- **CA-003.6** (RN-003.7) Dado um supervisor que errou o PIN 5 vezes seguidas, quando ele tenta liberar de novo dentro de 15 minutos, então a liberação é recusada mesmo com o PIN correto.
- **CA-003.7** (RN-003.8) Dado um supervisor operando o caixa sem permissão de executar uma operação, quando ele tenta autorizar a própria operação, então a liberação é recusada.
- **CA-003.8** (RN-003.6) Dado um usuário autenticado, quando ele cadastra um PIN com menos de 4 dígitos ou com letras, então o cadastro é recusado.
- **CA-003.9** (RN-003.10, RN-003.11) Dado um cancelamento liberado por um supervisor, quando o administrador consulta o registro de liberações, então encontra a operação com quem pediu, quem autorizou, caixa, venda, valor e horário.
- **CA-003.10** (RN-003.3) Dado uma loja recém-criada, quando o administrador abre a configuração de permissões, então vê a configuração padrão.

## Fora de escopo

- Liberação remota pelo painel ou celular do supervisor.
- Liberação por crachá ou biometria.
- Sangria e suprimento (entrarão como operações sensíveis quando existirem).

## Dependências

- RF-002: a pessoa no caixa é a pessoa autenticada no dispositivo pareado, e o fechamento com diferença acima da tolerância usa esta liberação.

## Decisões

- **D-003.1** (2026-10-16) Modelo de mercado em quatro peças, sem a liberação remota nesta versão: permissão por operação e por perfil, liberação presencial com credencial do supervisor, registro de auditoria e (no RF-002) fechamento com conferência cega.
- **D-003.2** (2026-10-16) A credencial de liberação é um PIN pessoal, não a senha da conta: é rápido no balcão e não expõe a senha principal.

## Decisões em aberto

1. **Perfis:** quais perfis existem (hoje há os papéis de conta `ADMIN` e `OPERATOR` e os níveis do cadastro de operador). Sugestão: perfis da loja (Operador, Supervisor, Gerente, Administrador) atribuídos à conta, substituindo os níveis fixos.
2. **Identificação do autorizador** na liberação: código do operador, e-mail ou escolha numa lista dos autorizadores da loja.
3. **Desconto:** limite por perfil (por exemplo, operador dá até 5% sem liberação) entra nesta versão ou fica para depois?

## Histórico

| Versão | Data | Mudança |
|---|---|---|
| 1.0 | 2026-10-16 | Rascunho do levantamento |
