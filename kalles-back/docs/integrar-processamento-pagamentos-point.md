# Integrar o processamento de pagamentos

O processamento de pagamentos com o Mercado Pago Point, integrado ao ponto de venda, é realizado por meio da criação de orders que incluem uma transação de pagamento associada. Ao criar uma order, ela é automaticamente enviada ao terminal Point indicado, permitindo que o comprador realize o pagamento de forma presencial.

Esta integração permite criar, processar e cancelar orders, além de realizar reembolsos e consultar informações e atualizações de _status_ das transações.

> NOTE
>
> Para definir se o parcelamento será com ou sem juros, é necessário configurar essa opção previamente na sua conta do Mercado Pago, antes da criação da order. Consulte as instruções para configurar o [parcelamento com](https://www.mercadopago.com.br/ajuda/24694) ou [sem juros](/developers/pt/support/oferecer-parcelas-sem-acrescimo-para-compradores_454).

:::AccordionComponent{title="Criar uma order"}
Para começar a processar pagamentos com o Point a partir dos pontos de venda, primeiro você precisa identificar a qual terminal a order será atribuída. Lembre-se que este terminal deverá ter sido [configurado em modo PDV](/developers/pt/reference/in-person-payments/point/terminals/update-operation-mode/patch).

Para isso, envie um **GET** para o endpoint :TagComponent{tag="API" text="Obter lista de terminals" href="/developers/pt/reference/in-person-payments/point/terminals/get-terminals/get"}, utilizando seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark*acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."}.

Caso necessário, é possível filtrar a busca usando os _query params_ opcionais `store_id` e `pos_id`, que correspondem aos identificadores da loja e do caixa retornados na resposta à criação de cada um.

```curl
curl -X GET \
  'https://api.mercadopago.com/terminals/v1/list?limit=50&offset=0&store_id=12354567&pos_id=23545678' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer TEST-232********97488-12********26f67454********f4c8b49c********9526408'
```

A resposta dessa solicitação exibirá os terminals associados à sua conta, permitindo que você selecione aquele que deseja usar para criar sua order.

A identificação do terminal pode ser feita pelos últimos caracteres do campo `id`, que correspondem ao número de série impresso na etiqueta traseira do terminal físico.

```json
{
  "data": {
    "terminals": [
      {
        "id": "NEWLAND_N950__N950NCB801293324",
        "pos_id": "23545678",
        "store_id": "12354567",
        "external_pos_id": "SUC0101POS",
        "operating_mode": "PDV"
      }
    ]
  },
  "paging": {
    "total": 1,
    "offset": 0,
    "limit": 50
  }
}
```

Em seguida, você deve criar a order. Para isso, envie um **POST** para o endpoint :TagComponent{tag="API" text="/v1/orders" href="/developers/pt/reference/in-person-payments/point/orders/create-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark*acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."}, e o id do terminal ao qual deseja atribuir a order, obtido no passo anterior.

```curl
curl -X POST \
  'https://api.mercadopago.com/v1/orders' \
  -H 'Content-Type: application/json' \
  -H 'X-Idempotency-Key: 0d5020ed-1af6-469c-ae06-c3bec19954bb' \
  -H 'Authorization: Bearer TEST-232********97488-12********26f67454********f4c8b49c********9526408' \
  -d '{
  "type": "point",
  "external_reference": "ext_ref_1234",
  "expiration_time": "PT16M",
  "transactions": {
  "payments": [
  {
  "amount": "24.00"
  }
  ]
  },
  "config": {
  "point": {
  "terminal_id": "NEWLAND_N950__N950NCB801293324",
  "print_on_terminal": "no_ticket"
  },
  "payment_method": {
  "default_type": "credit_card",
  "default_installments": 6,
  "installments_cost": "seller"
  }
  },
  "description": "Point Smart 2",
  "integration_data": {
  "platform_id": "dev_1234567890",
  "integrator_id": "dev_1234567890",
  "sponsor": {
  "id": "446566691"
  }
  }
  }'
```

Consulte na tabela abaixo as descrições dos parâmetros que possuem alguma particularidade importante que deve ser destacada.

| Atributo                      | Tipo           | Descrição                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | Obrigatoriedade |
| ----------------------------- | -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- |
| `Authorization`               | _Header_       | Refere-se ao :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark*acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."}. | Obrigatório     |
| `X-Idempotency-Key`           | _Header_       | Chave de idempotência. Essa chave garante que cada solicitação seja processada apenas uma vez, evitando duplicidades. Use um valor exclusivo no `header` da requisição, como um UUID V4 ou uma _string_ aleatória.                                                                                                                                                                                                                                                                                                                                                                                                          | Obrigatório     |
| `type`                        | _Body.String_  | Tipo de order, associado à solução do Mercado Pago para a qual está sendo criada. Para pagamentos com Mercado Pago Point, o único valor possível é `point`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | Obrigatório     |
| `external_reference`          | _Body.String_  | É uma referência externa da order, atribuída no momento de sua criação. Deve ser um valor único para cada order e não pode conter dados PII. O limite máximo permitido é de 64 caracteres e os permitidos são: **letras maiúsculas e minúsculas**, **números** e **os símbolos de hífen (-) e sublinhado(\_)**.                                                                                                                                                                                                                                                                                                             | Obrigatório     |
| `expiration_time`             | _Body. String_ | Indica o **período de validade** da order de pagamento a partir de sua criação. Durante esse tempo, a order estará disponível para ser processada pelo cliente; caso não seja processada dentro do prazo especificado, expirará automaticamente e não poderá ser utilizada, sendo necessário gerar uma nova order de pagamento para continuar. O valor mínimo permitido é de 30 segundos (PT30S) e o máximo é de 3 horas (PT3H). Exemplos de uso: para uma expiração de 30 segundos: "PT30S", para 10 minutos: "PT10M" e para 1 hora e 15 minutos: "PT1H15M".                                                               | Opcional        |
| `transaction.payments.amount` | _Body.String_  | Valor total da order de pagamento. O campo deve obrigatoriamente conter 2 casas decimais, mesmo quando for um número inteiro (por exemplo, "10.00").                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | Obrigatório     |
| `config.point.terminal_id`    | _Body.String_  | Identificador do terminal Point que receberá a order. Você deve enviá-lo exatamente como foi retornado na requisição :TagComponent{tag="API" text="Obter terminals" href="/developers/pt/reference/in-person-payments/point/terminals/get-terminals/get"}, como no seguinte exemplo: "NEWLAND_N950\_\_N950NCB801293324.                                                                                                                                                                                                                                                                                                     | Obrigatório     |

> NOTE
>
> Para mais detalhes sobre os parâmetros que devem enviados nesta requisição, consulte nossa [Referência de API](/developers/pt/reference/in-person-payments/point/orders/create-order/post).

Se a solicitação for bem-sucedida, a resposta retornará uma order com _status_ `created`.

```json
{
  "id": "ORD00001111222233334444555566",
  "type": "point",
  "user_id": "5238400195",
  "external_reference": "ext_ref_1234",
  "description": "Point Smart 2",
  "expiration_time": "PT16M",
  "processing_mode": "automatic",
  "country_code": "BRA",
  "integration_data": {
    "application_id": "1234567890",
    "platform_id": "dev_1234567890",
    "integrator_id": "dev_1234567890",
    "sponsor": {
      "id": "446566691"
    }
  },
  "status": "created",
  "status_detail": "created",
  "created_date": "2024-09-10T14:26:42.109320977Z",
  "last_updated_date": "2024-09-10T14:26:42.109320977Z",
  "config": {
    "point": {
      "terminal_id": "NEWLAND_N950__N950NCB801293324",
      "print_on_terminal": "no_ticket"
    },
    "payment_method": {
      "default_type": "credit_card",
      "default_installments": "6",
      "installments_cost": "seller"
    }
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01J67CQQH5904WDBVZEM4JMEP3",
        "amount": "24.00",
        "status": "created"
      }
    ]
  }
}
```

> NOTE
>
> Como a order é a base do processamento do pagamento, é importante armazenar os dados retornados na sua criação, principalmente seu `id` e o `id` do pagamento (`transactions.payments.id`). Esses identificadores serão necessários para realizar outras operações e consultar notificações de forma adequada. Além disso, você pode consultar nossa documentação na **seção Recursos** para entender melhor sobre os [possíveis _status_ de uma order e de uma transação](/developers/pt/docs/mp-point/resources/status-order-transaction).

Esta order será recebida automaticamente pelo terminal ao qual foi atribuída. Caso a order não seja carregada automaticamente no terminal, pressione o botão **Atualizar** ou, se o terminal possuir, o **botão verde** para receber a order. Assim, o pagamento poderá ser realizado pelo comprador no terminal e, em seguida, processado. **Lembre-se de que, se você não preencher o parâmetro `expiration_time`, o pagamento deverá ser realizado em até 15 minutos após a criação da order; após esse período, a order expirará.**

:::
:::::AccordionComponent{title="Cancelar uma order"}
O cancelamento de uma order pode ser feito de duas formas, a depender do _status_ em que ela se encontra.

- Se o `status` da order for `created`, o cancelamento deve ser feito via API.
- Se o `status` for `at_terminal`, isso indica que a order já foi recebida pelo terminal e deverá ser cancelada a partir dele.

> WARNING
>
> Caso o parâmetro `expiration_time` não seja preenchido, se a order não for processada em **até 15 minutos após sua criação**, seu `status` passará a ser `expired` e não será mais possível cancelá-la.
> <br>
> Além disso, no caso de cancelamentos a partir do terminal, é importante ter previamente configurado suas [notificações Webhooks](/developers/pt/docs/mp-point/notifications) para receber o aviso do cancelamento em seu sistema, o que permitirá manter sua conciliação.

Escolha a opção que melhor se adequa às suas necessidades para saber como cancelar sua order.

::::TabsComponent

:::TabComponent{title="Via API"}
Para cancelar uma order com _status_ `created`, envie um **POST** para o endpoint :TagComponent{tag="API" text="/v1/orders/{order*id}/cancel" href="/developers/pt/reference/in-person-payments/point/orders/cancel-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark_acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."}. Também é necessário enviar o `id` da order que deseja cancelar, obtido na resposta à sua criação.

```curl
curl -X POST \
  'https://api.mercadopago.com/v1/orders/ORDER_ID/cancel' \
  -H 'Content-Type: application/json' \
  -H 'X-Idempotency-Key: 0d5020ed-1af6-469c-ae06-c3bec19954bb' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Se a solicitação for bem-sucedida, a resposta mostrará um `status=canceled`.

```json
{
  "id": "ORD0000ABCD222233334444555566",
  "user_id": "5238400195",
  "type": "point",
  "external_reference": "ext_ref_1234",
  "description": "Point Smart 2",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "processing_mode": "automatic",
  "integration_data": {
    "application_id": "1234567890",
    "platform_id": "dev_1234567890",
    "integrator_id": "dev_1234567890",
    "sponsor": {
      "id": "446566691"
    }
  },
  "status": "canceled",
  "status_detail": "canceled",
  "created_date": "2024-09-10T14:26:42.109320977Z",
  "last_updated_date": "2024-09-10T14:26:42.109320977Z",
  "config": {
    "point": {
      "terminal_id": "NEWLAND_N950__N950NCB801293324",
      "print_on_terminal": "no_ticket"
    },
    "payment_method": {
      "default_type": "credit_card",
      "default_installments": "6",
      "installments_cost": "seller"
    }
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01J67CQQH5904WDBVZEM4JMEP3",
        "amount": "24.00",
        "status": "canceled",
        "status_detail": "canceled_by_api"
      }
    ]
  }
}
```

:::
:::TabComponent{title="A partir do terminal"}

Para cancelar uma order com status `at_terminal`, é preciso considerar o modelo de terminal que está utilizando.

- **Cancelar order na Point Smart 1 e Point Smart 2** <br><br>

  Como a order é obtida automaticamente pelo terminal, para cancelá-la, é necessário sair da tela sem finalizar o pagamento. <br><br>

  Para isso, pressione o **botão inferior direito** no terminal. Em seguida, ao aparecer a pergunta se deseja sair sem finalizar, selecione a opção **Sim**. <br><br>

  Dessa forma, a ordar será cancelada nos seus terminals Point Smart. <br><br>

- **Cancelar order na Point Pro 2 e Point Pro 3** <br><br>

  Como a order é obtida automaticamente pelo terminal, para cancelá-la, mantenha o **botão vermelho** do terminal pressionado. <br><br>

  Quando aparecer a mensagem de confirmação, selecione a opção **Sim** para sair da tela. <br><br>

Ao finalizar o cancelamento via terminal, e uma vez configuradas suas [notificações Webhooks](/developers/pt/docs/mp-point/notifications), você receberá o aviso em seu sistema, o que permitirá manter sua conciliação.

:::
::::
:::::

:::::AccordionComponent{title="Reembolsar uma order"}

É possível reembolsar uma order criada por meio da nossa API. Neste caso, o reembolso será sempre uma devolução total do valor da order.

> WARNING
>
> Uma order poderá ser reembolsada via API **até 90 dias após o pagamento ter sido realizado**. Após esse período, não será mais possível fazer a devolução.

Para realizar o reembolso de uma order, envie um **POST** para o endpoint :TagComponent{tag="API" text="/v1/orders/{order*id}/refund" href="/developers/pt/reference/in-person-payments/point/orders/refund-order/post"} **sem enviar o body** na requisição. Certifique-se de incluir seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark_acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."}. Também é necessário informar o `id` da order que deseja reembolsar, obtido na resposta à sua criação.

```curl
curl -X POST \
  'https://api.mercadopago.com/v1/orders/ORDER_ID/refund' \
  -H 'Content-Type: application/json' \
  -H 'X-Idempotency-Key: 0d5020ed-1af6-469c-ae06-c3bec19954bb' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Se a solicitação for bem-sucedida, a resposta trará o `status=refunded` e um novo nó `transactions.refunds`, que irá conter os detalhes do reembolso, além do `id` do pagamento original e o `id` da transação de reembolso.

```json
{
  "id": "ORD0000ABCD222233334444555566",
  "status": "refunded",
  "status_detail": "refunded",
  "transactions": {
    "refunds": [
      {
        "id": "REF01J67CQQH5904WDBVZEM1234D",
        "transaction_id": "PAY01J67CQQH5904WDBVZEM4JMEP3",
        "reference_id": "12345678",
        "amount": "38.00",
        "status": "processed"
      }
    ]
  }
}
```

:::::

:::::AccordionComponent{title="Consultar dados de uma order"}
Se necessário, você pode consultar os dados de uma order e suas transações associadas, sejam pagamentos ou reembolsos, incluindo seus _status_ ou valores.

Embora o uso recorrente desta consulta via API **não seja recomendado**, ela pode ser útil caso você precise de informações adicionais sobre o pedido.

Para realizar a consulta, envie um **GET** para o endpoint :TagComponent{tag="API" text="/v1/orders/{order*id}" href="/developers/pt/reference/in-person-payments/point/orders/get-order/get"}, incluindo o :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark_acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."}, além do `id` da order obtido na resposta à sua criação.

> WARNING
>
> Esta solicitação está disponível apenas para **orders criadas há menos de 3 meses**. Para acessar informações de orders mais antigas, é necessário entrar em contato com nosso serviço de atendimento ao cliente.

```curl
curl -X GET \
  'https://api.mercadopago.com/v1/orders/ORDER_ID' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Se a solicitação for bem-sucedida, a resposta retornará todas as informações da order, incluindo seu _status_, o _status_ do pagamento e/ou o _status_ do reembolso em tempo real:

```json
{
  "id": "ORD00001111222233334444555566",
  "user_id": "5238400195",
  "type": "point",
  "external_reference": "ext_ref_1234",
  "processing_mode": "automatic",
  "description": "Point Smart 2",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "integration_data": {
    "application_id": "1234567890",
    "platform_id": "dev_1234567890",
    "integrator_id": "dev_1234567890",
    "sponsor": {
      "id": "446566691"
    }
  },
  "status": "refunded",
  "status_detail": "refunded",
  "created_date": "2024-09-10T14:26:42.109320977Z",
  "last_updated_date": "2024-09-10T14:26:42.109320977Z",
  "config": {
    "point": {
      "terminal_id": "NEWLAND_N950__N950NCB801293324",
      "print_on_terminal": "no_ticket"
    },
    "payment_method": {
      "default_type": "credit_card",
      "default_installments": "6",
      "installments_cost": "seller"
    }
  },
  "transactions": {
    "payments": [
      {
        "id": "PAY01J67CQQH5904WDBVZEM4JMEP3",
        "amount": "24.00",
        "refunded_amount": "38.00",
        "tip_amount": "14.00",
        "paid_amount": "38.00",
        "status": "refunded",
        "status_detail": "created",
        "reference_id": "12345678",
        "payment_method": {
          "type": "credit_card",
          "installments": 1,
          "id": "master"
        }
      }
    ],
    "refunds": [
      {
        "id": "REF01J67CQQH5904WDBVZEM1234D",
        "transaction_id": "PAY01J67CQQH5904WDBVZEM4JMEP3",
        "reference_id": "12345678",
        "amount": "38.00",
        "status": "processed"
      }
    ]
  }
}
```

:::::
