# Realizar compra teste

O processo de teste varia de acordo com o modelo de Código QR integrado. Selecione o modelo correspondente e siga os passos a seguir para simular um fluxo completo de processamento utilizando o Código QR.

> NOTE
>
> O modelo híbrido contempla tanto o código estático quanto o dinâmico, permitindo que ambos os fluxos de teste sejam realizados nas integrações desse modelo.

:::::TabsComponent

::::TabComponent{title="Modelo estático"}
Siga os passos descritos em cada cenário de teste para simular um fluxo de processamento com um código QR de modelo estático.

:::AccordionComponent{title="Criar e processar uma order"}

Para testar a correta criação de uma order e o processamento da transação associada, siga os passos a seguir.

1. Crie uma loja e caixa certificando-se de utilizar seu **Access Token de teste** ao realizar as solicitações necessárias. Além disso, certifique-se de definir o campo `fixed_amount=true` ao criar o caixa. Os links na resposta do objeto QR fornecem as imagens do código QR do seu caixa. Para mais detalhes, consulte a documentação [Criar loja e caixa](/developers/pt/docs/qr-code/create-store-and-pos).

2. Ainda utilizando seu **Access Token** de teste, envie um **POST** ao endpoint [/v1/orders](/developers/pt/reference/in-person-payments/qr-code/orders/create-order/post) certificando-se de incluir o `external_pos_id` do caixa criado no passo anterior. Para mais detalhes, consulte a documentação [Integrar o processamento de pagamentos > Criar uma order](/developers/pt/docs/qr-code/payment-processing).

3. Armazene o identificador da order, retornado na resposta à sua criação sob o parâmetro `id`, para poder verificar o funcionamento correto das suas notificações webhooks.

4. Baixe o aplicativo do Mercado Pago no seu dispositivo móvel, disponível para [Android](https://play.google.com/store/apps/details?id=com.mercadopago.wallet&pcampaignid=web_share) e [iOS](https://apps.apple.com/br/app/mercado-pago-banco-digital/id925436649), instale-o e, em seguida, inicie sessão com as credenciais da **conta de teste comprador**. Se, ao iniciar sessão com uma conta de teste ou navegar pelas seções de **Suas integrações**, for solicitada autenticação por e-mail, acesse nossa documentação para saber como [validar o início de sessão em contas de teste](/developers/pt/docs/your-integrations/test/accounts#bookmark_validar_início_de_sessão_com_usuários_de_teste).

5. Use seu dispositivo móvel para escanear o código QR gerado anteriormente. O aplicativo mostrará o valor do pedido e as opções de pagamento disponíveis.

6. Realize o pagamento utilizando os cartões de teste disponíveis na conta do usuário de teste comprador, considerando os seguintes cenários.

> WARNING
>
> Se você estiver utilizando a [conta de teste comprador](/developers/pt/docs/qr-code/test-integration#bookmark_obter_uma_conta_de_teste_comprador) criada automaticamente com sua aplicação, os cartões de teste já estarão disponíveis para utilização. Para contas de teste comprador antigas, será necessário adicionar manualmente os [cartões de teste](/developers/pt/docs/qr-code/resources/test-cards) para testar diferentes cenários de pagamento.

| Tipo de cartão | Bandeira   | Número              | Código de segurança | Data de vencimento | Comportamento                                  |
| -------------- | ---------- | ------------------- | ------------------- | ------------------ | ---------------------------------------------- |
| Crédito        | Mastercard | 5031 4332 1540 6351 | 123                 | 11/30              | Processa um pagamento aprovado                 |
| Crédito        | Visa       | 4235 4777 2802 5682 | 123                 | 11/30              | Processa um pagamento aprovado                 |
| Débito         | Elo Débito | 5067 7667 8388 8311 | 123                 | 11/30              | Processa um pagamento rejeitado                |
| Crédito        | Visa       | 4174 0005 1758 0553 | 123                 | 4/44               | Processa um pagamento aprovado sem exigir CVV. |

7. Verifique se recebeu a notificação webhook do Mercado Pago para o processamento da order. O campo `action`, terá o valor `order.processed`, que indica que a order foi processada, e dentro do nó `transactions.payments` você poderá ver o status da transação, como mostra o exemplo de notificação webhook abaixo.

```json
{
  "action": "order.processed",
  "api_version": "v1",
  "application_id": "7364289770550796",
  "data": {
    "external_reference": "ER_123456",
    "id": "ORD01JV3AW3NFSTSTB669F41NACDX",
    "status": "processed",
    "status_detail": "accredited",
    "total_amount": "30.00",
    "total_paid_amount": "30.00",
    "transactions": {
      "payments": [
        {
          "amount": "30.00",
          "id": "PAY01JV3AW3NFSTSTB669F4JSAA6C",
          "paid_amount": "30.00",
          "payment_method": {
            "id": "account_money",
            "installments": 1,
            "type": "account_money"
          },
          "reference": {
            "id": "92937960454"
          },
          "status": "processed",
          "status_detail": "accredited"
        }
      ]
    },
    "type": "qr",
    "version": 2
  },
  "date_created": "2025-05-12T22:46:59.635090485Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1403498245"
}
```

:::

:::AccordionComponent{title="Reembolsar uma order"}

Para confirmar que o fluxo de processamento funciona corretamente, você poderá realizar um reembolso da transação processada no passo anterior.

Para isso, envie um **POST** ao endpoint [Reembolsar uma order](/developers/pt/reference/in-person-payments/qr-code/orders/refund-order/post), certificando-se de incluir o `id` da order que deseja reembolsar e que foi obtido na resposta à sua criação. Se tiver dúvidas sobre como fazê-lo, acesse a seção [Integrar o processamento de pagamentos > Reembolsar uma order](/developers/pt/docs/qr-code/payment-processing).

Por último, verifique se recebeu a notificação webhook do Mercado Pago correspondente a essa transação, que deverá indicar no campo `action` o valor `order.refunded` e também o status da transação reembolsada.

```json
{
  "action": "order.refunded",
  "api_version": "v1",
  "application_id": "7364289770550796",
  "data": {
    "external_reference": "ER_123456",
    "id": "ORD01JV3AW7R6WME2XT0KZRX7HVS6",
    "status": "refunded",
    "status_detail": "refunded",
    "total_amount": "30.00",
    "total_paid_amount": "30.00",
    "type": "qr",
    "version": 3
  },
  "date_created": "2025-05-12T22:47:05.813331521Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1403498245"
}
```

:::
:::AccordionComponent{title="Criar e cancelar uma order"}

Para validar o funcionamento do fluxo de cancelamento, siga os passos a seguir:

1. O cancelamento de uma order de teste só é possível quando ela está com o status `created`. Portanto, crie uma nova order enviando um **POST** ao endpoint [/v1/orders](/developers/pt/reference/in-person-payments/qr-code/orders/create-order/post).
2. Faça uma solicitação para [Cancelar order por ID](/developers/pt/reference/in-person-payments/qr-code/orders/cancel-order/post) incluindo o `id` da order obtida na resposta à sua criação, feita no passo anterior.
3. Por último, verifique se recebeu a notificação webhook do Mercado Pago correspondente a essa transação, que deverá indicar no campo `action` o valor `order.canceled` e também o status da transação cancelada.

```json
{
  "action": "order.canceled",
  "api_version": "v1",
  "application_id": "7364289770550796",
  "data": {
    "external_reference": "ER_123456",
    "id": "ORD01JV3AW2C31TE7FY2C4VHTJKB2",
    "status": "canceled",
    "status_detail": "canceled",
    "total_amount": "30.00",
    "type": "qr",
    "version": 2
  },
  "date_created": "2025-05-12T22:46:57.697535027Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1403498245"
}
```

:::
::::

::::TabComponent{title="Modelo dinâmico"}

Siga os passos descritos em cada cenário de teste para simular um fluxo de processamento com um código QR de modelo dinâmico.

:::AccordionComponent{title="Criar e processar uma order"}

Para testar a criação correta de uma order e o processamento da transação associada, siga os passos a seguir.

1. Crie uma loja e caixa certificando-se de usar seu _Access Token_ de teste ao realizar as solicitações necessárias. Além disso, certifique-se de definir o campo `fixed_amount=true` ao criar o caixa. Para mais detalhes, consulte a documentação [Criar loja e caixa](/developers/pt/docs/qr-code/create-store-and-pos).

2. Ainda utilizando seu **Access Token** de teste, envie um **POST** ao endpoint [/v1/orders](/developers/pt/reference/in-person-payments/qr-code/orders/create-order/post) certificando-se de incluir o `external_pos_id` do caixa criado no passo anterior. Para mais detalhes, consulte a documentação [Integrar o processamento de pagamentos > Criar uma order](/developers/pt/docs/qr-code/payment-processing).

3. Utilize o parâmetro `qr_data` obtido na resposta à criação do pedido para gerar um código QR. Você pode utilizar ferramentas ou bibliotecas que o ajudarão a converter essa _string_ em uma imagem de código QR.

4. Armazene o identificador da order, retornado na resposta à sua criação sob o parâmetro `id`, para poder verificar o funcionamento correto das suas notificações webhooks.

5. Baixe o aplicativo do Mercado Pago no seu dispositivo móvel, disponível para [Android](https://play.google.com/store/apps/details?id=com.mercadopago.wallet&pcampaignid=web_share) e [iOS](https://apps.apple.com/br/app/mercado-pago-banco-digital/id925436649), instale-o e, em seguida, inicie sessão com as credenciais da **conta de teste comprador**. Se, ao iniciar sessão com uma conta de teste ou navegar pelas seções de **Suas integrações**, for solicitada autenticação por e-mail, acesse nossa documentação para saber como [validar o início de sessão em contas de teste](/developers/pt/docs/your-integrations/test/accounts#bookmark_validar_início_de_sessão_com_usuários_de_teste).

6. Use seu dispositivo móvel para escanear o código QR gerado anteriormente. O aplicativo mostrará o valor do pedido e as opções de pagamento disponíveis.

7. Realize o pagamento utilizando os cartões de teste disponíveis na conta do usuário de teste comprador, considerando os seguintes cenários.

> WARNING
>
> Se você estiver utilizando a [conta de teste comprador](/developers/pt/docs/qr-code/test-integration#bookmark_obter_uma_conta_de_teste_comprador) criada automaticamente com sua aplicação, os cartões de teste já estarão disponíveis para utilização. Para contas de teste comprador antigas, será necessário adicionar manualmente os [cartões de teste](/developers/pt/docs/qr-code/resources/test-cards) para testar diferentes cenários de pagamento.

| Tipo de cartão | Bandeira   | Número              | Código de segurança | Data de vencimento | Comportamento                                  |
| -------------- | ---------- | ------------------- | ------------------- | ------------------ | ---------------------------------------------- |
| Crédito        | Mastercard | 5031 4332 1540 6351 | 123                 | 11/30              | Processa um pagamento aprovado                 |
| Crédito        | Visa       | 4235 4777 2802 5682 | 123                 | 11/30              | Processa um pagamento aprovado                 |
| Débito         | Elo Débito | 5067 7667 8388 8311 | 123                 | 11/30              | Processa um pagamento rejeitado                |
| Crédito        | Visa       | 4174 0005 1758 0553 | 123                 | 4/44               | Processa um pagamento aprovado sem exigir CVV. |

8. Verifique se recebeu a notificação webhook do Mercado Pago para o processamento da order e do pagamento. O campo `action`, terá o valor `order.processed`, que indica que a order foi processada, e dentro do nó `transactions.payments` você poderá ver o status da transação, como mostra o exemplo de notificação webhook abaixo.

```json
{
  "action": "order.processed",
  "api_version": "v1",
  "application_id": "7364289770550796",
  "data": {
    "external_reference": "ER_123456",
    "id": "ORD01JV3AW3NFSTSTB669F41NACDX",
    "status": "processed",
    "status_detail": "accredited",
    "total_amount": "30.00",
    "total_paid_amount": "30.00",
    "transactions": {
      "payments": [
        {
          "amount": "30.00",
          "id": "PAY01JV3AW3NFSTSTB669F4JSAA6C",
          "paid_amount": "30.00",
          "payment_method": {
            "id": "account_money",
            "installments": 1,
            "type": "account_money"
          },
          "reference": {
            "id": "92937960454"
          },
          "status": "processed",
          "status_detail": "accredited"
        }
      ]
    },
    "type": "qr",
    "version": 2
  },
  "date_created": "2025-05-12T22:46:59.635090485Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1403498245"
}
```

:::
:::AccordionComponent{title="Reembolsar uma order"}

Para confirmar que o fluxo de processamento funciona corretamente, você poderá realizar um reembolso da transação processada no passo anterior.

Para isso, envie um **POST** ao endpoint [Reembolsar uma order](/developers/pt/reference/in-person-payments/qr-code/orders/refund-order/post), certificando-se de incluir o `id` da order que deseja reembolsar e que foi obtido na resposta à sua criação. Se tiver dúvidas sobre como fazê-lo, acesse a seção [Integrar o processamento de pagamentos > Reembolsar uma order](/developers/pt/docs/qr-code/payment-processing).

Por último, verifique se recebeu a notificação webhook do Mercado Pago correspondente a essa transação, que deverá indicar no campo `action` o valor `order.refunded` e também o status da transação reembolsada.

```json
{
  "action": "order.refunded",
  "api_version": "v1",
  "application_id": "7364289770550796",
  "data": {
    "external_reference": "ER_123456",
    "id": "ORD01JV3AW7R6WME2XT0KZRX7HVS6",
    "status": "refunded",
    "status_detail": "refunded",
    "total_amount": "30.00",
    "total_paid_amount": "30.00",
    "type": "qr",
    "version": 3
  },
  "date_created": "2025-05-12T22:47:05.813331521Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1403498245"
}
```

:::

:::AccordionComponent{title="Criar e cancelar uma order"}

Para validar o funcionamento do fluxo de cancelamento, siga os passos a seguir:

1. O cancelamento de uma order de teste só é possível quando ela está com o status `created`. Portanto, crie uma nova order enviando um **POST** ao endpoint [/v1/orders](/developers/pt/reference/in-person-payments/qr-code/orders/create-order/post).
2. Faça uma solicitação para [Cancelar order por ID](/developers/pt/reference/in-person-payments/qr-code/orders/cancel-order/post) incluindo o `id` da order obtida na resposta à sua criação, feita no passo anterior.
3. Por último, verifique se recebeu a notificação webhook do Mercado Pago correspondente a essa transação, que deverá indicar no campo `action` o valor `order.canceled` e também o status da transação cancelada.

```json
{
  "action": "order.canceled",
  "api_version": "v1",
  "application_id": "7364289770550796",
  "data": {
    "external_reference": "ER_123456",
    "id": "ORD01JV3AW2C31TE7FY2C4VHTJKB2",
    "status": "canceled",
    "status_detail": "canceled",
    "total_amount": "30.00",
    "type": "qr",
    "version": 2
  },
  "date_created": "2025-05-12T22:46:57.697535027Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1403498245"
}
```

:::
::::
:::::

Depois de testar todos os cenários e verificar o funcionamento correto da sua integração com código QR, você poderá [subir em produção](/developers/pt/docs/qr-code/go-to-production).
