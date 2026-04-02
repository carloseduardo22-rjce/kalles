# Status de uma order e de uma transação

Ao processar orders com o Mercado Pago Point, é importante entender como funciona o fluxo desse processamento, e quais são os _status_ que uma order e uma transação podem assumir em cada momento.

A seguir, você pode visualizar o fluxo de de uma order para pagamentos com cartão e, em seguida, uma tabela informativa sobre cada um dos possíveis `status`, incluindo os respectivos `status_detail`.

<pre class="mermaid">
stateDiagram-v2
  [*] --> created

  created --> expired: Não foi processada em até 15 minutos após sua criação
  created --> canceled: Cancelado via API
  created --> at_terminal

  at_terminal --> canceled: Cancelado via terminal
  at_terminal --> processed: Processamento bem-sucedido
  at_terminal --> failed: Pagamento rejeitado / Falha no componente de pagamento / Reverso EMV
  at_terminal --> action_required: após 40s do início do processamento
  at_terminal --> expired: Expirou enquanto estava no terminal

  processed --> refunded: Reembolso total via API, atividade ou terminal

  created: created
  expired: expired
  canceled: canceled
  at_terminal: at_terminal
  processed: processed
  failed: failed
  action_required: action_required
  refunded: refunded
</pre>

## Status da order

Veja a lista de `status` e `status_detail` que uma order pode assumir.

| `status`          | `status_detail`   | Descrição                                                                                                                                                                                                                |
| ----------------- | ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `created`         | `created`         | A order foi criada com sucesso, mas ainda não foi capturada pela terminal.                                                                                                                                               |
| `processed`       | `processed`       | A order foi processada com sucesso e o pagamento foi acreditado.                                                                                                                                                         |
| `action_required` | `action_required` | A transação associada à order precisa ser confirmada. Verifique a terminal para identificar o _status_ final e certifique-se de atualizar seu sistema com base nessa informação, pois esse _status_ não será modificado. |
| `at_terminal`     | `at_terminal`     | A order foi capturada pela terminal e está pronta para ser processada.                                                                                                                                                   |
| `failed`          | `failed`          | A order falhou. Isso significa que a transação não foi bem-sucedida e não será concluída.                                                                                                                                |
| `refunded`        | `refunded`        | A order foi reembolsada. Isso significa que o valor da transação foi integralmente devolvido ao pagador.                                                                                                                 |
| `expired`         | `expired`         | A order expirou após mais de 15 minutos sem pagamento. Caso deseje realizar o pagamento, será necessário criar uma nova order.                                                                                           |

## Status da transação

Veja a lista de status e `status_detail` que uma transação pode assumir.

| `status`          | `status_detail`               | Descrição                                                                                                                                                                                                                                                                 |
| ----------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `created`         | `created`                     | A transação foi criada com sucesso, mas ainda não foi processada. Este é o _status_ inicial de uma transação após a sua criação.                                                                                                                                          |
| `processed`       | `accredited`                  | A transação foi processada com sucesso e o valor foi efetivamente compensado.                                                                                                                                                                                             |
| `action_required` | `waiting_payment`             | A transação requer uma ação adicional e está aguardando o pagamento. Isso significa que a transação foi iniciada, mas o pagamento ainda não foi concluído.                                                                                                                |
| `action_required` | `check_on_terminal`           | A transação requer uma confirmação na terminal para verificar se o pagamento foi aprovado ou recusado.                                                                                                                                                                    |
| `at_terminal`     | `at_terminal`                 | A transação foi capturada pela terminal e está pronta para ser processada.                                                                                                                                                                                                |
| `expired`         | `expired`                     | A transação expirou. Isso significa que não foi concluída dentro do tempo limite e, portanto, foi encerrada.                                                                                                                                                              |
| `refunded`        | `refunded`                    | A order foi reembolsada. Isso significa que o valor da transação foi devolvido integralmente ao pagador.                                                                                                                                                                  |
| `canceled`        | `canceled`                    | A transação foi cancelada e não será concluída.                                                                                                                                                                                                                           |
| `canceled`        | `canceled_by_api`             | A transação foi cancelada via API e não será concluída.                                                                                                                                                                                                                   |
| `canceled`        | `canceled_on_terminal`        | A transação foi cancelada diretamente na terminal e não será concluída.                                                                                                                                                                                                   |
| `failed`          | `failed`                      | Houve uma falha no processamento da order. Isso pode ter ocorrido por envio de dados incorretos, risco de fraude ou recusa pela entidade emissora do meio de pagamento.                                                                                                   |
| `failed`          | `bad_filled_card_data`        | A transação falhou devido ao preenchimento incorreto dos dados do cartão. Isso pode incluir informações como número do cartão, CVV, data de validade, entre outros.                                                                                                       |
| `failed`          | `high_risk`                   | A transação falhou devido à detecção de alto risco. Isso pode ocorrer quando o sistema antifraude identifica um possível risco na transação.                                                                                                                              |
| `failed`          | `rejected_by_issuer`          | A transação falhou devido à recusa por parte do emissor do cartão.                                                                                                                                                                                                        |
| `failed`          | `required_call_for_authorize` | A transação falhou porque é necessária uma chamada para autorização. Isso pode ocorrer quando o emissor do cartão exige uma verificação adicional antes de aprovar a transação.                                                                                           |
| `failed`          | `max_attempts_exceeded`       | A transação falhou porque foi excedido o número máximo de tentativas. Isso pode ocorrer quando o número de tentativas de pagamento ultrapassa o limite permitido pelo sistema.                                                                                            |
| `failed`          | `card_disabled`               | A transação falhou porque o cartão está desativado. Isso pode acontecer quando o cartão foi bloqueado ou desativado pelo emissor.                                                                                                                                         |
| `failed`          | `insufficient_amount`         | A transação falhou devido a valor insuficiente. Isso pode ocorrer quando o saldo disponível não é suficiente para cobrir o valor da transação.                                                                                                                            |
| `failed`          | `amount_limit_exceeded`       | A transação falhou porque o valor excedeu o limite permitido. Isso pode acontecer quando o valor da transação ultrapassa o limite definido pelo emissor do cartão ou pelo sistema.                                                                                        |
| `failed`          | `processing_error`            | A transação falhou por erro de processamento. Isso pode ocorrer por problemas técnicos ou falhas no sistema que impedem a finalização da transação. Se o problema persistir, entre em contato com o suporte e forneça o `x-request-id` junto com os detalhes da operação. |
| `failed`          | `invalid_installments`        | A transação falhou devido a parcelas inválidas. Isso pode ocorrer quando o número de parcelas selecionadas não é aceito pelo emissor do cartão ou pelo sistema.                                                                                                           |
| `failed`          | `in_review`                   | A transação falhou e seu status é desconhecido ou contém informações sensíveis.                                                                                                                                                                                           |
