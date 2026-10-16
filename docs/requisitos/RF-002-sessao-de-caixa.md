# RF-002 — Sessão de caixa

| Versão | Estado | Data |
|---|---|---|
| 1.0 | Rascunho | 2026-10-16 |

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

- **RN-002.1** Caixa é um dispositivo pareado: um navegador ou máquina vinculado a um cadastro de caixa da loja pelo administrador.
- **RN-002.2** O pareamento é feito pelo administrador: ele gera um código de pareamento para um caixa e o código é informado no dispositivo. O código vale por tempo limitado e uma única vez.
- **RN-002.3** Um caixa tem no máximo um dispositivo pareado ativo. Parear de novo revoga o pareamento anterior.
- **RN-002.4** O administrador pode revogar o pareamento de um caixa a qualquer momento; o dispositivo deixa de poder abrir ou operar sessão.
- **RN-002.5** Sessão de caixa só é aberta e operada no dispositivo pareado com aquele caixa.
- **RN-002.6** O operador da sessão é sempre a pessoa autenticada no dispositivo. Ninguém abre sessão em nome de outra pessoa.
- **RN-002.7** Podem abrir sessão contas com papel de operador e o administrador. O administrador opera em nome próprio, com as mesmas regras do operador.
- **RN-002.8** Um caixa tem no máximo uma sessão aberta, e uma pessoa tem no máximo uma sessão aberta.
- **RN-002.9** A abertura registra o fundo de troco informado (valor maior ou igual a zero).
- **RN-002.10** O fechamento é feito por conferência cega: o operador informa o valor contado em dinheiro sem ver o valor esperado; o sistema calcula e registra a diferença.
- **RN-002.11** Se a diferença, em valor absoluto, passar da tolerância configurada pela loja, o fechamento exige liberação de supervisor (RF-003).
- **RN-002.12** A tela de Caixas da administração acompanha os caixas e as sessões (situação, operador, abertura, dispositivo pareado); ela não abre sessão.

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

## Fora de escopo

- Sangria e suprimento (retirada e reforço de dinheiro durante a sessão).
- Identificação do dispositivo por certificado ou instalação nativa.
- Fechamento forçado de sessão abandonada pela administração.

## Dependências

- RF-003 (autorização de supervisor) para o fechamento com diferença acima da tolerância.

## Decisões

- **D-002.1** (2026-10-16) Caixa é dispositivo pareado, aproveitando o pareamento que já existe no back-end.
- **D-002.2** (2026-10-16) O operador da sessão é a pessoa autenticada; o cadastro de operador passa a ser ligado a uma conta.
- **D-002.3** (2026-10-16) O administrador pode operar o caixa em nome próprio, num dispositivo pareado.
- **D-002.4** (2026-10-16) Fechamento com conferência cega; supervisor só é chamado quando a diferença passa da tolerância.

## Decisões em aberto

1. **Tolerância padrão** de diferença no fechamento (sugestão: R$ 0,00 até a loja configurar outro valor).
2. **Validade do código de pareamento** (sugestão: 10 minutos) e **duração da credencial do dispositivo** depois de pareado (hoje 7 dias; sugestão: sem prazo, até ser revogada ou substituída).
3. **Cadastros de operador existentes**: como ligar cada `Operator` a uma conta (criar conta para cada um ou exigir vínculo manual).
4. **Sessão abandonada**: se o operador sai sem fechar, quem fecha e como (hoje fora de escopo; confirmar).

## Histórico

| Versão | Data | Mudança |
|---|---|---|
| 1.0 | 2026-10-16 | Rascunho do levantamento |
