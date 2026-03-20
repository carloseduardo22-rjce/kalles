# Integrar o processamento de pagamentos

O processamento de pagamentos com código QR é realizado por meio da criação de orders que incluem uma transação de pagamento associada. Ao criar uma order, o comprador poderá realizar o pagamento de forma presencial escaneando o código.

Existem três modelos de Código QR disponíveis para integração, definidos no momento da criação da order:

- **Modelo estático**: Neste modelo, um único código QR associado ao caixa criado previamente recebe as informações de cada order gerada.
- **Modelo dinâmico**: Um código QR exclusivo e de pagamento único é gerado para cada transação, contendo os dados específicos da order criada.
- **Modelo híbrido**: Permite que o pagamento seja realizado tanto pelo QR estático quanto pelo dinâmico. A order é vinculada ao código QR estático do caixa, enquanto também é gerado um QR dinâmico simultaneamente. Uma vez que o pagamento seja realizado com qualquer um dos dois códigos, o outro ficará automaticamente desabilitado para uso.

Esta integração permite criar, processar e cancelar orders, além de realizar reembolsos e consultar informações e atualizações de status das transações.

> NOTE
>
> Para definir se o parcelamento será com ou sem juros, é necessário configurar essa opção previamente na sua conta do Mercado Pago, antes da criação da order. Consulte as instruções para configurar o parcelamento [com](https://www.mercadopago.com.br/ajuda/24694) ou [sem juros](/developers/pt/support/oferecer-parcelas-sem-acrescimo-para-compradores_454).

:::::AccordionComponent{title="Criar uma order"}
Para configurar o processamento de pagamentos com Código QR, é necessário identificar a loja e o caixa aos quais a order será associada. Lembre-se de que tanto a loja quanto o caixa devem ter sido [criados previamente](/developers/pt/docs/qr-code/create-store-and-pos).

Em seguida, você poderá criar a order. Para isso, envie uma solicitação **POST** ao endpoint :TagComponent{tag="API" text="/v1/orders" href="/developers/pt/reference/in-person-payments/qr-code/orders/create-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Além disso, certifique-se de incluir o `external_pos_id` do caixa ao qual deseja atribuir a order, obtido na etapa anterior.

```curl
curl -X POST \
  'https://api.mercadopago.com/v1/orders'\
  -H 'Content-Type: application/json' \
  -H 'X-Idempotency-Key: 0d5020ed-1af6-469c-ae06-c3bec19954bb' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -d '{
  "type": "qr",
  "total_amount": 50.00,
  "description": "Smartphone",
  "external_reference": "ext_ref_1234",
  "config": {
  "qr": {
  "external_pos_id": "STORE001POS001",
  "mode": "static"
  }
  },
  "transactions": {
  "payments": [
  {
  "amount": 50.00
  }
  ]
  },
  "items": [
  {
  "title": "Smartphone",
  "unit_price": 50.00,
  "unit_measure": "kg",
  "external_code": "777489134",
  "quantity": 1,
  "external_categories": [
  {
  "id": "device"
  }
  ]
  }
  ],
  "discounts": {
  "payment_methods": [
  {
  "type": "account_money",
  "new_total_amount": 47.28
  }
  ]
  }
}'
```

| Parâmetro                      | Tipo     | Descrição                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         | Obrigatoriedade |
| ------------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- |
| `X-Idempotency-Key`            | _header_ | Chave de idempotência. Esta chave garante que cada solicitação seja processada apenas uma vez, evitando duplicidades. Utilize um valor exclusivo no `header` da solicitação, como um UUID (Universally Unique Identifier - Identificador Universalmente Único) V4 ou uma _string_ aleatória.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | Obrigatório     |
| `type`                         | _string_ | Tipo de order associada à solução do Mercado Pago para a qual foi criada. Para pagamentos com Código QR do Mercado Pago, o único valor possível é _qr_.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | Obrigatório     |
| `total_amount`                 | _string_ | Valor total da order. Representa a soma das transações. Pode conter dois decimais ou nenhum. Exemplo: 50.00.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | Opcional        |
| `description`                  | _string_ | Descrição do produto ou serviço. O limite máximo é de 150 caracteres e não pode ser utilizada para enviar dados PII.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | Opcional        |
| `external_reference`           | _string_ | É a referência externa da order, atribuída no momento da criação. O limite máximo permitido é de 64 caracteres e os permitidos são: letras maiúsculas e minúsculas, números e os símbolos hífen (-) e sublinhado (\_). O campo não pode ser utilizado para enviar dados PII. Além disso, esse valor deve ser único para cada order, pois atua como identificador dessa order.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Obrigatório     |
| `expiration_time`              | _string_ | Permite especificar a data de validade da order; ou seja, o tempo máximo que pode ficar ativa. O formato válido é ISO 8601, formato de duração. Por exemplo, "P3Y6M4DT12H30M5S" representa uma duração de 3 anos, 6 meses, 4 dias, 12 horas, 30 minutos e 5 segundos. O tempo mínimo que pode ser definido é 30 segundos e o máximo é 3600 horas. Se não for enviado, o valor padrão será 15 minutos.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | Opcional        |
| `config.qr.external_pos_id`    | _string_ | Identificador externo do caixa, definido pelo integrador durante sua criação. Ao incluí-lo, a informação da order fica associada ao caixa e à loja previamente criados dentro do sistema Mercado Pago. Importante: O campo `external_pos_id` deve ter o mesmo valor definido como `external_id` na criação do seu caixa.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | Obrigatório     |
| `mode`                         | _string_ | Modo de código QR associado à order. Os valores possíveis estão listados abaixo e, se nenhum for enviado, o valor padrão será `static`. <br> `static`: Modo estático, em que o código QR estático associado ao caixa definido no campo `external_pos_id` recebe a informação da order. <br>`dynamic`: Modo dinâmico, em que um código QR único é gerado para cada transação, incluindo os dados específicos da order criada. Este código deve ser construído a partir da informação retornada no campo `qr_data` da resposta, cujo valor é exclusivo para cada order. <br>`hybrid`: Permite que o pagamento seja realizado usando qualquer um dos dois modos, estático ou dinâmico, já que a order será vinculada ao código QR estático associado ao caixa (`external_pos_id`), e um QR será gerado dinamicamente em paralelo. No entanto, apenas um dos QR gerados poderá ser pago pelo cliente. | Opcional        |
| `transactions.payments`        | _array_  | O nó _transactions_ contém informações sobre a transação associada à order. Quando o `type` for `qr`, podem ser incluídas transações de pagamento _payments_, que por sua vez contêm informações sobre a order de pagamento. Apenas uma transação de pagamento pode ser enviada por order.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | Obrigatório     |
| `transactions.payments.amount` | _string_ | Valor do pagamento. Pode conter dois decimais ou nenhum. Exemplo: 50.00.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | Obrigatório     |

> NOTE
>
> Para mais detalhes sobre os parâmetros que devem ser enviados nesta solicitação, consulte nossa [Referência de API](/developers/pt/reference/in-person-payments/qr-code/orders/create-order/post).

A resposta varia conforme o modelo de QR escolhido para a integração. Selecione abaixo a opção que corresponde ao seu caso.

::::TabsComponent

:::TabComponent{title="Modelo estático"}
Ao criar uma order especificando o campo `config.qr.mode` como `static`, o QR que deverá ser escaneado pelo cliente é **o obtido na resposta à solicitação de criação da caixa**, pois é esse que receberá as informações da order criada. Se a solicitação for bem-sucedida, a resposta retornará uma order com status `created`.

Confira abaixo um exemplo de resposta para uma solicitação de criação de uma order para pagamentos com código QR estático.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code/test-integration).

```json
{
  "id": "ORD01K371WBFDS4MD9JG0K8ZMECBE",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ext_ref_1234",
  "description": "Smartphone",
  "total_amount": "50.00",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "user_id": "{{USER_ID}}",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-08-21T19:32:21.621Z",
  "last_updated_date": "2025-08-21T19:32:21.621Z",
  "integration_data": {
    "application_id": "{{APPLICATION_ID}}"
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01K371WBFDS4MD9JG0KCV6PRKQ",
        "amount": "50.00",
        "status": "created",
        "status_detail": "ready_to_process"
      }
    ]
  },
  "config": {
    "qr": {
      "external_pos_id": "STORE001POS001",
      "mode": "static"
    }
  },
  "items": [
    {
      "title": "Smartphone",
      "unit_price": "50.00",
      "unit_measure": "kg",
      "external_code": "777489134",
      "quantity": 1,
      "external_categories": [
        {
          "id": "device"
        }
      ]
    }
  ],
  "discounts": {
    "payment_methods": [
      {
        "type": "account_money",
        "new_total_amount": "47.28"
      }
    ]
  }
}
```

> WARNING
>
> Armazene o `id` da order e o `id` do pagamento (`transactions.payments.id`) retornados na criação. Eles são necessários para operações futuras e para validar notificações. Consulte **Recursos** para mais [detalhes sobre status da order e transação](/developers/pt/docs/qr-code/resources/status-order-transaction).

A order criada será automaticamente vinculada ao caixa especificado na solicitação, permitindo que o comprador realize o pagamento no ponto de venda físico. Além disso, a vinculação também facilita a conciliação. Após o pagamento, a transação será processada de forma integrada.

:::

:::TabComponent{title="Modelo dinâmico"}
Ao criar uma order especificando o modo `dynamic` no campo `config.qr.mode`, a resposta da solicitação incluirá o campo adicional `type_response.qr_data`. Este campo contém uma _string_ no formato [EMVCo](https://www.emvco.com/emv-technologies/qr-codes/), que pode ser convertida em um código QR para ser impresso ou exibido em uma tela ou dispositivo. Se a solicitação for bem-sucedida, a resposta retornará uma order com status `created`.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code/test-integration).

Consulte abaixo um exemplo de resposta para uma solicitação de criação de uma order para pagamentos com **código QR dinâmico**.

```json
{
  "id": "ORD01K372G4J4FXZ9HGHZMJMGGPKE",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ext_ref_1234",
  "description": "Smartphone",
  "total_amount": "50.00",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "user_id": "{{USER_ID}}",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-08-21T19:43:10.13Z",
  "last_updated_date": "2025-08-21T19:43:10.13Z",
  "integration_data": {
    "application_id": "{{APPLICATION_ID}}"
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01K372G4J4FXZ9HGHZMKWSQS20",
        "amount": "50.00",
        "status": "created",
        "status_detail": "ready_to_process"
      }
    ]
  },
  "config": {
    "qr": {
      "external_pos_id": "STORE001POS001",
      "mode": "dynamic"
    }
  },
  "type_response": {
    "qr_data": "00020101021226580014br.gov.bcb.qr01368ee55a9c-7db3-41e0-a8cd-fbff4d4765b5204000053039865802BR5925PABLO JOSE DE OLIVEIRA CA6009SAO PAULO61088051040062070503***630442E4"
  },
  "items": [
    {
      "title": "Smartphone",
      "unit_price": "50.00",
      "unit_measure": "kg",
      "external_code": "777489134",
      "quantity": 1,
      "external_categories": [
        {
          "id": "device"
        }
      ]
    }
  ],
  "discounts": {
    "payment_methods": [
      {
        "type": "account_money",
        "new_total_amount": "47.28"
      }
    ]
  }
}
```

> WARNING
>
> Armazene o `id` da order e o `id` do pagamento (`transactions.payments.id`) retornados na criação. Eles são necessários para operações futuras e para validar notificações. Consulte **Recursos** para mais [detalhes sobre status da order e transação](/developers/pt/docs/qr-code/resources/status-order-transaction).

Neste modelo, um código QR exclusivo é gerado para cada `order` criada, incorporando os dados específicos da transação. Após o pagamento, a transação é processada de forma integrada.

:::

:::TabComponent{title="Modelo híbrido"}
Ao criar uma order especificando o modo `hybrid` no campo `config.qr.mode`, a resposta da solicitação inclui o campo adicional `type_response.qr_data`. Assim como no modelo dinâmico, o valor deste campo contém uma _string_ no formato [EMVCo](https://www.emvco.com/emv-technologies/qr-codes/), que pode ser convertida em um código QR para impressão e pagamento pelo cliente.

Além disso, o cliente também poderá escanear o código QR obtido na resposta da solicitação de criação da caixa para realizar o pagamento, como ocorre no modelo estático, pois é esse que receberá as informações da order criada.

Dessa forma, o pagamento pode ser feito tanto pelo **QR estático do caixa** quanto por um **QR dinâmico** gerado ao mesmo tempo. A order é sempre vinculada ao QR estático, mas o cliente pode optar por usar qualquer um dos dois. Assim que o pagamento é concluído em um deles, o outro é automaticamente desabilitado, evitando duplicidade de transações.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code/test-integration).

Se a solicitação for bem-sucedida, a resposta retornará uma order com `status created`. Veja abaixo um exemplo de resposta para uma solicitação de criação de uma order para pagamentos com **código QR modelo híbrido**.

```json
{
  "id": "ORD01K37A6R7EAD3BQQJZJD4Q5K0E",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ext_ref_1234",
  "description": "Smartphone",
  "total_amount": "50.00",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "user_id": "{{USER_ID}}",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-08-21T20:21:43.987Z",
  "last_updated_date": "2025-08-21T20:21:43.987Z",
  "integration_data": {
    "application_id": "{{APPLICATION_ID}}"
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01K37A6R7EAD3BQQJZK3PKA90",
        "amount": "50.00",
        "status": "created",
        "status_detail": "ready_to_process"
      }
    ]
  },
  "config": {
    "qr": {
      "external_pos_id": "STORE001POS001",
      "mode": "hybrid"
    }
  },
  "type_response": {
    "qr_data": "00020101021226580014br.gov.bcb.qr01363f78b8c2-6f94-4c67-b593-4aad44e2ec51204000053039865802BR5925PABLO JOSE DE OLIVEIRA CA6009SAO PAULO61088051040062070503***6304CAC0"
  },
  "items": [
    {
      "title": "Smartphone",
      "unit_price": "50.00",
      "unit_measure": "kg",
      "external_code": "777489134",
      "quantity": 1,
      "external_categories": [
        {
          "id": "device"
        }
      ]
    }
  ],
  "discounts": {
    "payment_methods": [
      {
        "type": "account_money",
        "new_total_amount": "47.28"
      }
    ]
  }
}
```

> WARNING
>
> Armazene o `id` da order e o `id` do pagamento (`transactions.payments.id`) retornados na criação. Eles são necessários para operações futuras e para validar notificações. Consulte **Recursos** para mais [detalhes sobre status da order e transação](/developers/pt/docs/qr-code/resources/status-order-transaction).

:::

:::::

:::AccordionComponent{title="Cancelar order"}
O cancelamento de uma order só pode ser realizado quando seu `status` for `created`. Se a solicitação de cancelamento for feita com outro status, a API retornará um erro informando o conflito.

Para cancelar uma order, envie um **POST** para o endpoint :TagComponent{tag="API" text="/v1/orders/{order_id}/cancel" href="/developers/pt/reference/in-person-payments/qr-code/orders/cancel-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Também é necessário enviar o `id` da order que deseja cancelar, obtido na resposta à sua criação.

```curl
curl --location --request PUT 'https://api.mercadopago.com/v1/orders/ORD01K371WBFDS4MD9JG0K8ZMECBE' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}' \
--data-raw '{
  "status": "canceled"
}'
```

Se a solicitação for bem-sucedida, a resposta incluirá o campo `status` com o valor `canceled`.

```json
{
  "id": "ORD01K371WBFDS4MD9JG0K8ZMECBE",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ext_ref_1234",
  "description": "Smartphone",
  "total_amount": "50.00",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "user_id": "{{USER_ID}}",
  "status": "canceled",
  "status_detail": "canceled",
  "currency": "BRL",
  "created_date": "2025-08-21T19:32:21.621Z",
  "last_updated_date": "2025-08-21T19:33:52.012Z",
  "integration_data": {
    "application_id": "{{APPLICATION_ID}}"
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01K371WBFDS4MD9JG0KCV6PRKQ",
        "amount": "50.00",
        "status": "canceled",
        "status_detail": "canceled_by_api"
      }
    ]
  },
  "config": {
    "qr": {
      "external_pos_id": "STORE001POS001",
      "mode": "static"
    }
  },
  "items": [
    {
      "title": "Smartphone",
      "unit_price": "50.00",
      "unit_measure": "kg",
      "external_code": "777489134",
      "quantity": 1,
      "external_categories": [
        {
          "id": "device"
        }
      ]
    }
  ],
  "discounts": {
    "payment_methods": [
      {
        "type": "account_money",
        "new_total_amount": "47.28"
      }
    ]
  }
}
```

:::

:::AccordionComponent{title="Reembolsar uma order"}

É possível reembolsar uma order criada por meio da nossa API. Neste caso, o reembolso será sempre uma devolução total do valor da order. Para efetuar o reembolso de uma order via API, ela deve estar com o `status processed`. Se o status for diferente, a API retornará uma mensagem de erro indicando o conflito.

> WARNING
>
> Uma order poderá ser reembolsada via API até **180 dias após a realização do pagamento**. Após esse período, não será possível efetuar a devolução.

Para realizar o reembolso de uma order, envie um **POST** para o endpoint :TagComponent{tag="API" text="/v1/orders/{order_id}/refund" href="/developers/pt/reference/in-person-payments/qr-code/orders/refund-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Também é necessário informar o `id` da order que deseja reembolsar, obtido na resposta à sua criação.

```curl
curl --location --request POST 'https://api.mercadopago.com/v1/orders/ORDER_ID/refunds' \
--header 'Content-Type: application/json' \
--header 'X-Idempotency-Key: 91b59be9-27b8-449f-a6bd-32dca8b424cd' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Se a solicitação for bem-sucedida, a resposta trará o `status accredited` e um novo nó `transactions.refunds`, que conterá os detalhes do reembolso, além do `id` do pagamento original e o `id` da transação de reembolso.

```json
{
  "id": "ORD01JV391F8YM8EDEAG8CWZ0GM0N",
  "status": "processed",
  "status_detail": "accredited",
  "transactions": {
    "refunds": [
      {
        "id": "REF01JW7YS4YHV543DJ6JGYZBX6A0",
        "transaction_id": "PAY01JVMKQQE6YW97CEQN3MJ666HD",
        "reference_id": "{{REFERENCE_ID}}",
        "amount": "50.00",
        "status": "processing"
      }
    ]
  }
}
```

Na resposta da solicitação de reembolso, é criada uma nova transação do tipo `refund` com `status processing`. Para acompanhar o status final do reembolso, aguarde a notificação de atualização ou consulte os dados da order para verificar seu status. Quando o reembolso for confirmado, o status será alterado para `refunded`.

:::

:::AccordionComponent{title="Consultar dados de uma order"}
É possível consultar os dados de uma order e suas transações associadas, sejam pagamentos ou reembolsos, incluindo seus status ou valores.

Para realizar a consulta, envie um **GET** ao endpoint :TagComponent{tag="API" text="/v1/orders/{order_id}" href="/developers/pt/reference/in-person-payments/qr-code/orders/get-order/get"} incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Além disso, certifique-se de incluir o `id` da order obtido na resposta à sua criação.

```curl
curl --location --request GET 'https://api.mercadopago.com/v1/orders/ORDER_ID' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

> WARNING
>
> Esta solicitação está disponível apenas para orders criadas há menos de 3 meses. Para acessar informações de orders mais antigas, é necessário contatar nosso atendimento ao cliente.

Se a solicitação for bem-sucedida, a resposta retornará toda a informação da order, incluindo seu status, o status do pagamento e/ou o status do reembolso em tempo real.

```json
{
  "id": "ORD01K371WBFDS4MD9JG0K8ZMECBE",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ext_ref_1234",
  "description": "Smartphone",
  "total_amount": "50.00",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "user_id": "{{USER_ID}}",
  "status": "canceled",
  "status_detail": "canceled",
  "currency": "BRL",
  "created_date": "2025-08-21T19:32:21.621Z",
  "last_updated_date": "2025-08-21T19:33:52.012Z",
  "integration_data": {
    "application_id": "{{APPLICATION_ID}}"
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01K371WBFDS4MD9JG0K8ZMECBE",
        "amount": "50.00",
        "status": "canceled",
        "status_detail": "canceled_by_api"
      }
    ]
  },
  "config": {
    "qr": {
      "external_pos_id": "STORE001POS001",
      "mode": "static"
    }
  },
  "items": [
    {
      "title": "Smartphone",
      "unit_price": "50.00",
      "unit_measure": "kg",
      "external_code": "777489134",
      "quantity": 1,
      "external_categories": [
        {
          "id": "device"
        }
      ]
    }
  ],
  "discounts": {
    "payment_methods": [
      {
        "type": "account_money",
        "new_total_amount": "47.28"
      }
    ]
  }
}
```

:::

Após a integração do processamento de pagamentos, você poderá [configurar as notificações](/developers/pt/docs/qr-code/notifications).
