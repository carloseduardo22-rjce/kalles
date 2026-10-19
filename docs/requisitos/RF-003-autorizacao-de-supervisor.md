# RF-003 — Autorização de supervisor no caixa

| Versão | Estado | Data |
|---|---|---|
| 1.0 | Aprovado | 2026-10-19 |

## História de usuário

Como gerente da loja, quero que as operações sensíveis do caixa só aconteçam com permissão de quem pode autorizá-las, comprovada no momento e registrada, para controlar perdas e saber quem liberou cada operação.

## Situação atual

- Quatro operações dependem de autorização: remover item da venda, cancelar venda, aplicar desconto manual em item e fechar o caixa.
- A permissão é uma escada fixa de níveis do cadastro de operador (`BASIC` < `SUPERVISOR` < `MANAGER` < `ADMIN`). Remover item, cancelar venda e dar desconto: nível `SUPERVISOR` ou acima executa direto; abaixo disso, precisa de alguém de nível maior. Fechar caixa: precisa de um autorizador `SUPERVISOR` ou acima.
- A autorização é feita informando só o código do operador autorizador (por exemplo `OP-002`), sem senha ou PIN. Quem souber o código de um supervisor autoriza qualquer operação.
- As autorizações não ficam registradas de forma consultável.

## Regras de negócio

- **RN-003.0** Toda conta da loja tem um de quatro perfis:

  | Perfil | Quem é na loja | O que faz |
  |---|---|---|
  | Operador | Quem fica no caixa | Abre e fecha a própria sessão, registra vendas, recebe pagamentos, identifica o cliente e dá desconto até o limite do perfil |
  | Supervisor | Quem atende os caixas no salão ("fiscal de caixa") | Libera as operações sensíveis dos operadores; pode operar um caixa |
  | Gerente | Responsável pela loja | Tudo do supervisor, mais cadastros, preços, estoque, relatórios, pareamento de caixas, encerramento administrativo de sessão e configuração de tolerância, limites e permissões da loja |
  | Administrador | Dono do negócio | Tudo do gerente em todas as lojas, mais assinatura, integrações, criação de lojas e de contas de gerente |

  Os quatro perfis substituem os papéis de conta atuais (`ADMIN`, `OPERATOR`) e os níveis do cadastro de operador (`BASIC`, `SUPERVISOR`, `MANAGER`, `ADMIN`).
- **RN-003.1** As operações sensíveis do caixa formam uma lista fechada: remover item, cancelar venda, desconto manual acima do limite do perfil, fechar caixa com diferença acima da tolerância e encerramento administrativo de sessão (RF-002).
- **RN-003.2** Cada perfil de acesso define, para cada operação sensível, se quem tem o perfil pode **executar sem liberação** e se pode **autorizar** a operação de outra pessoa. As duas permissões são independentes.
- **RN-003.3** A loja configura as permissões dos perfis. Uma loja nova começa com uma configuração padrão: operador não executa nem autoriza; supervisor executa e autoriza; administrador executa e autoriza tudo.
- **RN-003.4** Quando a pessoa no caixa não pode executar a operação sem liberação, o caixa pede a liberação de alguém que possa autorizá-la.
- **RN-003.5** A liberação é presencial, no próprio caixa: o autorizador informa a sua matrícula (código do operador) e o seu PIN pessoal.
- **RN-003.6** O PIN é pessoal, numérico, de 4 a 6 dígitos, cadastrado pela própria pessoa e armazenado só como hash. Ninguém consegue ver o PIN de outra pessoa.
- **RN-003.7** Depois de 5 PINs errados seguidos, a liberação por aquela pessoa fica bloqueada por 15 minutos.
- **RN-003.8** Uma pessoa não autoriza a própria operação.
- **RN-003.9** Cada liberação vale para uma única operação.
- **RN-003.10** Toda tentativa de liberação, aceita ou recusada, é registrada com: operação, quem pediu, quem autorizou (ou tentou), caixa, sessão, venda e item quando houver, valor envolvido, data e hora e resultado.
- **RN-003.11** Gerente (da sua loja) e administrador consultam o registro de liberações filtrando por período, caixa, pessoa e operação.
- **RN-003.12** Cada perfil tem um desconto manual máximo, em percentual do valor do item. Até o limite, quem tem o perfil aplica o desconto sem liberação; acima dele, precisa de liberação de quem tenha limite suficiente. Padrão de loja nova: Operador 0%, Supervisor 10%, Gerente e Administrador 100%.
- **RN-003.13** As permissões são verificadas no back-end em toda operação. Esconder ou desabilitar a ação na tela é só conveniência e não substitui a verificação.

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
- **CA-003.11** (RN-003.12) Dado um operador com limite de desconto de 5%, quando ele aplica 5% num item, então o desconto é aplicado sem liberação; quando aplica 8%, então o caixa pede liberação.
- **CA-003.12** (RN-003.12) Dado um pedido de liberação de desconto de 15%, quando quem libera é um supervisor com limite de 10%, então a liberação é recusada.
- **CA-003.13** (RN-003.13) Dado um operador sem permissão de cancelar venda, quando o cancelamento é enviado direto à API sem liberação, então é recusado com status 403.
- **CA-003.14** (RN-003.0) Dado um operador autenticado, quando ele acessa a configuração de produtos, então o acesso é recusado.

## Fora de escopo

- Liberação remota pelo painel ou celular do supervisor.
- Liberação por crachá ou biometria.
- Sangria e suprimento (entrarão como operações sensíveis quando existirem).
- Perfis personalizados criados pela loja.
- Desconto máximo por produto (há sistemas, como o Alterdata, que combinam limite do produto e do usuário).

## Dependências

- RF-002: a pessoa no caixa é a pessoa autenticada no dispositivo pareado, e o fechamento com diferença acima da tolerância usa esta liberação.

## Decisões

- **D-003.1** (2026-10-16) Modelo em quatro peças, sem liberação remota nesta versão: permissão por operação e por perfil, liberação presencial com credencial do supervisor, registro de auditoria e (no RF-002) fechamento com conferência cega.
- **D-003.2** (2026-10-16) A credencial de liberação é um PIN pessoal, não a senha da conta. No Brasil é comum pedir usuário e senha do gerente (Cplug, Omie); o PIN do gerente é o padrão em PDVs como o Magento POS. Escolhido o PIN por ser rápido no balcão e não expor a senha principal.
- **D-003.3** (2026-10-19) Quatro perfis fixos (Operador, Supervisor, Gerente, Administrador) com permissões ajustáveis pela loja; perfis personalizados ficam fora.
- **D-003.4** (2026-10-19) O autorizador se identifica pela matrícula (código do operador) e pelo PIN.
- **D-003.5** (2026-10-19) Desconto máximo em percentual por perfil nesta versão, como em Senior, Cplug e TOTVS.
- **D-003.6** (2026-10-19) Verificação das permissões no back-end (RN-003.13), seguindo o alerta de que bloquear só na tela é contornável.

## Referências de mercado

- Perfis e permissões: [Senior — Perfis de Usuários](https://documentacao.senior.com.br/gestaoempresarialerp/5.10.4/Menu_Cadastros/f090pfu.htm), [TOTVS — Conceder descontos (fiscal × operador)](https://tdn.totvs.com.br/download/attachments/203762607/Tutorial%20Conceder%20Descontos%20na%20Rotina%202075.pdf?api=v2), [Omie — cancelamento com senha de gerente ou supervisor](https://ajuda.omie.com.br/pt-BR/articles/8017614-omie-pdv-desktop-visualizando-e-cancelando-cupons-emitidos-no-pdv-desktop).
- Desconto máximo e senha de gerente: [Cplug — Balcão](https://manual.cplug.com.br/books/balc%C3%A3o/page/balc%C3%A3o---geral/export/pdf), [Alterdata — prioridade de desconto no PDV](https://ajuda.alterdata.com.br/shopbase/prioridade-de-desconto-no-pdv-253560058.html), [Alterdata — percentual máximo de descontos](https://ajuda.alterdata.com.br/x/fRJ6Ew).
- PIN do gerente, auditoria e verificação no servidor: [Magento 2 POS staff permissions](https://ecosire.com/apps/magento-2/magento-2-pos-staff-permissions-shifts), [Odoo POS access management](https://ecosire.com/fr/apps/odoo/simplify-pos-access-management).

## Histórico

| Versão | Data | Mudança |
|---|---|---|
| 1.0 | 2026-10-16 | Rascunho do levantamento |
| 1.0 | 2026-10-19 | Pontos em aberto decididos e requisito aprovado, perfis descritos, com referências de mercado |
