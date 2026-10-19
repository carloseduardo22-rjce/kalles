# RF-002 — Sessão de caixa

| Versão | Estado | Data |
|---|---|---|
| 1.0 | Aprovado | 2026-10-19 |

## História de usuário

Como operador de caixa, quero abrir, operar e fechar a minha sessão no caixa em que estou, para que cada venda e cada diferença de dinheiro fiquem registradas em nome de quem realmente operou aquele caixa.

## Situação atual

- O back-end já trata o caixa como dispositivo: o administrador gera um token de pareamento (`POST /api/pos/admin/generate-token`), o navegador do caixa o registra (`POST /api/pos/setup`, cookie `kalles_pos_token`) e quem faz login nesse navegador carrega o `posId` do caixa. Abrir sessão exige esse pareamento (`PairedDeviceSessionGuard`).
- O mesmo token serve de código de pareamento e de credencial do dispositivo, vale 7 dias e pode ser gerado várias vezes para o mesmo caixa, sem limite de dispositivos pareados.
- O front-end não tem tela de pareamento. Na prática, ninguém consegue abrir sessão pela interface; as sessões abertas que apareciam vinham dos dados de demonstração.
- A sessão é aberta informando um código de operador (`Operator`), cadastro separado da conta de login. Nada liga a pessoa autenticada ao operador da sessão.
- O fechamento exige o código de um operador com nível `SUPERVISOR` ou superior, sem senha ou PIN, e o valor contado em dinheiro.
- Até 15/10/2026 o PDV adotava qualquer sessão aberta da loja; hoje retoma só a sessão aberta no próprio navegador.

## Regras de negócio

- **RN-002.1** Caixa é um dispositivo pareado: um navegador ou máquina vinculado a um cadastro de caixa da loja pelo gerente ou administrador.
- **RN-002.2** O pareamento é feito pelo gerente ou administrador: ele gera um código de pareamento para um caixa e o código é informado no dispositivo. O código vale por 10 minutos e uma única vez.
- **RN-002.2a** Depois de pareado, o dispositivo continua autorizado sem prazo, até o pareamento ser revogado ou substituído.
- **RN-002.3** Um caixa tem no máximo um dispositivo pareado ativo. Parear de novo revoga o pareamento anterior.
- **RN-002.4** O gerente ou administrador pode revogar o pareamento de um caixa a qualquer momento; o dispositivo deixa de poder abrir ou operar sessão.
- **RN-002.5** Sessão de caixa só é aberta e operada no dispositivo pareado com aquele caixa.
- **RN-002.6** O operador da sessão é sempre a pessoa autenticada no dispositivo. Ninguém abre sessão em nome de outra pessoa.
- **RN-002.7** Podem abrir sessão contas com perfil Operador, Supervisor, Gerente ou Administrador (perfis definidos no RF-003). Quem não é Operador opera em nome próprio, com as mesmas regras do operador.
- **RN-002.7a** Todo operador de caixa é uma conta de acesso; o código do operador passa a ser a matrícula dessa conta. Cadastro de operador sem conta não abre sessão.
- **RN-002.8** Um caixa tem no máximo uma sessão aberta, e uma pessoa tem no máximo uma sessão aberta.
- **RN-002.9** A abertura registra o fundo de troco informado (valor maior ou igual a zero).
- **RN-002.10** O fechamento é feito por conferência cega: o operador informa o valor contado em dinheiro sem ver o valor esperado; o sistema calcula e registra a diferença.
- **RN-002.11** Se a diferença, em valor absoluto, passar da tolerância configurada pela loja, o fechamento exige liberação de supervisor (RF-003). A tolerância é configurada por loja; loja nova começa com R$ 0,00.
- **RN-002.12** A tela de Caixas da administração acompanha os caixas e as sessões (situação, operador, abertura, dispositivo pareado, conferência pendente); ela não abre sessão.
- **RN-002.13** Uma sessão deixada aberta pode ser encerrada pela tela de Caixas por quem tem permissão de encerramento administrativo (Gerente e Administrador, por padrão), com liberação por PIN (RF-003). O encerramento libera o caixa para nova abertura e fica registrado como encerramento administrativo, com quem encerrou e quando.
- **RN-002.14** No encerramento administrativo, o valor contado pode ser informado na hora ou depois. Enquanto não for informado, a sessão fica com **conferência pendente** e a diferença não é calculada; o sistema nunca assume o valor contado igual ao esperado.
- **RN-002.15** A conferência pendente é concluída na tela de Caixas por quem tem permissão de encerramento administrativo, informando o valor contado; a diferença é calculada e registrada nesse momento.

## Critérios de aceitação

- **CA-002.1** (RN-002.2) Dado um administrador na tela de Caixas, quando ele gera o código de pareamento de um caixa, então o código é exibido com o prazo de validade.
- **CA-002.2** (RN-002.2, RN-002.1) Dado um código de pareamento válido, quando ele é informado num navegador, então esse navegador passa a ser o dispositivo do caixa.
- **CA-002.3** (RN-002.2) Dado um código de pareamento já usado ou vencido, quando ele é informado, então o pareamento é recusado.
- **CA-002.4** (RN-002.3) Dado um caixa pareado com o dispositivo A, quando ele é pareado com o dispositivo B, então o dispositivo A não consegue mais abrir sessão nesse caixa.
- **CA-002.5** (RN-002.4) Dado um caixa pareado, quando o administrador revoga o pareamento, então o dispositivo não consegue mais abrir nem operar sessão.
- **CA-002.6** (RN-002.5) Dado um navegador não pareado, quando alguém tenta abrir sessão, então a abertura é recusada e o PDV orienta a parear o dispositivo.
- **CA-002.7** (RN-002.6) Dado um operador autenticado num dispositivo pareado, quando ele abre a sessão, então a sessão é registrada em nome dele, sem escolha de operador.
- **CA-002.8** (RN-002.7) Dado um administrador autenticado num dispositivo pareado, quando ele abre a sessão, então a sessão é registrada em nome do administrador.
- **CA-002.9** (RN-002.8) Dado um caixa com sessão aberta, quando outra pessoa tenta abrir sessão nele, então a abertura é recusada com status 409.
- **CA-002.10** (RN-002.8) Dado um operador com sessão aberta no caixa 1, quando ele tenta abrir sessão no caixa 2, então a abertura é recusada com status 409.
- **CA-002.11** (RN-002.10) Dado um operador fechando a sessão, quando ele chega à etapa de contagem, então o valor esperado não é exibido antes de ele informar o valor contado.
- **CA-002.12** (RN-002.10, RN-002.11) Dado um fechamento com diferença dentro da tolerância, quando o operador confirma, então a sessão é fechada sem liberação e a diferença fica registrada.
- **CA-002.13** (RN-002.11) Dado um fechamento com diferença acima da tolerância, quando o operador confirma, então o sistema pede liberação de supervisor antes de fechar.
- **CA-002.14** (RN-002.12) Dado um administrador na tela de Caixas, então ele vê cada caixa com situação, operador da sessão aberta e dispositivo pareado, e não há ação de abrir sessão.
- **CA-002.15** (RN-002.2a) Dado um dispositivo pareado há mais de 7 dias, sem revogação, quando o operador abre sessão nele, então a abertura é aceita.
- **CA-002.16** (RN-002.7a) Dado um cadastro de operador sem conta vinculada, quando se tenta abrir sessão em nome dele, então a abertura não é possível.
- **CA-002.17** (RN-002.13) Dado um caixa com sessão aberta e um gerente na tela de Caixas, quando ele encerra a sessão com PIN válido sem informar o valor contado, então a sessão é encerrada como administrativa com conferência pendente e o caixa pode ser aberto de novo.
- **CA-002.18** (RN-002.14) Dado um encerramento administrativo sem valor contado, então a sessão não mostra diferença de caixa e aparece como conferência pendente.
- **CA-002.19** (RN-002.15) Dado uma sessão com conferência pendente, quando o gerente informa o valor contado, então a diferença é calculada, registrada, e a pendência deixa de aparecer.
- **CA-002.20** (RN-002.13) Dado um operador na tela de Caixas, quando ele tenta encerrar a sessão de outra pessoa, então o encerramento é recusado.

## Fora de escopo

- Sangria e suprimento (retirada e reforço de dinheiro durante a sessão).
- Identificação do dispositivo por certificado ou instalação nativa.

## Dependências

- RF-003 (autorização de supervisor) para o fechamento com diferença acima da tolerância.

## Decisões

- **D-002.1** (2026-10-16) Caixa é dispositivo pareado, aproveitando o pareamento que já existe no back-end.
- **D-002.2** (2026-10-16) O operador da sessão é a pessoa autenticada; o cadastro de operador passa a ser ligado a uma conta.
- **D-002.3** (2026-10-16) Quem não é Operador pode operar o caixa em nome próprio, num dispositivo pareado.
- **D-002.4** (2026-10-16) Fechamento com conferência cega; supervisor só é chamado quando a diferença passa da tolerância.
- **D-002.5** (2026-10-19) Tolerância configurável por loja, com padrão R$ 0,00. Não há valor de mercado: é política de cada empresa.
- **D-002.6** (2026-10-19) Código de pareamento de 10 minutos e uso único; dispositivo autorizado sem prazo até revogação ou substituição. O uso único é mais rígido do que o Square, que permite reaproveitar o código no mesmo aparelho.
- **D-002.7** (2026-10-19) Todo operador é uma conta; o código do operador vira matrícula.
- **D-002.8** (2026-10-19) Sessão abandonada: encerramento administrativo com conferência pendente, combinando o encerramento pela retaguarda (Lightspeed, NCR Voyix, Enfore) com a pendência visível até a conferência (TOTVS, Alterdata). Rejeitado o modelo que assume valor contado igual ao esperado, porque esconde a quebra de caixa.

## Referências de mercado

- Conferência cega: [Alterdata Spice — Conferência Cega de Caixa](https://ajuda.alterdata.com.br/spicebase/spice-simples-conferencia-cega-de-caixa-340001450.html), [Alterdata Bimer — modelo operacional](https://ajuda.alterdata.com.br/bimerconfc/modelo-operacional-da-conferencia-cega-de-caixa).
- Pareamento: [Square — Set up device codes](https://squareup.com/help/article/8339), [Square Terminal API Quickstart](https://developer.squareup.com/docs/terminal-api/quickstart), [Square — Manage Devices by Location](https://square.com/help/ca/en/article/5586-manage-devices-by-location).
- Sessão abandonada: [Lightspeed — Opening & Closing Troubleshooting](https://product-info.lightspeedhq.com/opening-closing-troubleshooting-guide/), [NCR Voyix — Starting and ending a shift](https://docs.ncrvoyix.com/restaurant/silver-essentials/using/starting_and_ending_a_shift), [Enfore — Force Close Device Business Day](https://care-kb.enfore.com/hc/en/articles/10340562835357.html), [TOTVS — Abertura de Caixa com Conferência Pendente](https://tdn.totvs.com/download/attachments/46079061/LOJA-FRT_BT_Abertura%20de%20Caixa%20com%20Conf%20Pendente_BRA_TFWKGT.pdf?api=v2), [Alterdata — Abertura de Caixa](https://ajuda.alterdata.com.br/isxpu/abertura-de-caixa-227601760.html), [Odoo — pos_manager_close_session](https://apps.odoo.com/apps/modules/19.0/pos_manager_close_session).

## Histórico

| Versão | Data | Mudança |
|---|---|---|
| 1.0 | 2026-10-16 | Rascunho do levantamento |
| 1.0 | 2026-10-19 | Pontos em aberto decididos e requisito aprovado, com referências de mercado |
