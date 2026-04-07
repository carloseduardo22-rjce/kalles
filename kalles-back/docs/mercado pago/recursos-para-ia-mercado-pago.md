# Criar aplicação

As **aplicações** são entidades registradas dentro do Mercado Pago que atuam como um identificador único para gerenciar a autenticação e a autorização de suas integrações. Ou seja, são o vínculo entre seu desenvolvimento e o Mercado Pago e constituem como a primeira etapa para realizar a integração.

Com elas, é possível acessar as :toolTipComponent[credenciais]{link="/developers/pt/docs/credentials" linkText="Credenciais" content="Chaves de acesso únicas que usamos para identificar uma integração na sua conta, estando vinculadas à sua aplicação. Para mais informações, acesse o link abaixo."} necessárias para interagir com nossas APIs ou serviços específicos, além de gerenciar e organizar sua integração. Por isso, será necessário criar uma aplicação para cada solução do Mercado Pago integrada.

Para criar uma **aplicação**, siga os passos abaixo.

1. No canto superior direito de Mercado Pago Developers, clique em **Entrar** e faça login em sua conta do Mercado Pago.
2. Com a sessão iniciada, no canto superior direito do Mercado Pago Developers, clique em **Criar aplicação** se a sua conta ainda não tiver nenhuma aplicação criada, ou acesse "Suas integrações" e selecione **Ver todas**. Nela, clique em **Criar aplicação**.
3. Uma vez dentro de **Suas integrações**, clique no botão **Criar aplicação**.

> NOTE
>
> Para proteger sua conta e garantir a conformidade das operações, durante a criação de uma aplicação será necessário que realize uma verificação de identidade, caso ainda não tenha sido feita, ou uma reautenticação, se já tiver concluído previamente o processo de verificação.

![create-application-1](/images/snippets/create-application-1-pt-v1.png)

4. Insira um **nome** para identificar sua aplicação. O limite é de até 50 caracteres alfanuméricos.
5. Selecione **Pagamentos presenciais** como o tipo de pagamento que você deseja integrar, pois essa é a solução correspondente a lojas físicas. Clique em **Continuar**.
6. Em seguida, escolha **Código QR** como o tipo de pagamento que você deseja integrar e clique no botão **Continuar**.
7. Confirme as opções selecionadas. Caso precise modificar alguma seleção, clique no botão **Editar**. Aceite a [Declaração de Privacidade](https://www.mercadopago.com.br/privacidade) e os [Termos e Condições](/developers/pt/docs/resources/legal/terms-and-conditions) e clique em **Confirmar**.

![Resumo de aplicação](/images/snippets/create-application/PT-new-app-QR-Code-v1.png)

Em [Suas integrações](/developers/panel/app), é possível consultar a lista de todas as suas aplicações criadas e acessar os [Detalhes da aplicação](/developers/pt/docs/qr-code/resources/application-details) de cada uma delas.

> NOTE
>
> Caso necessário, é possível editar ou excluir uma aplicação. Neste último caso, tenha em mente que sua loja deixará de utilizar nossos recursos ou de receber pagamentos por meio da integração com o Mercado Pago associada a essa aplicação. Para mais informações, acesse os [Detalhes da aplicação](/developers/pt/docs/qr-code/resources/application-details).

## Acessar as credenciais de teste

Depois de criar sua aplicação, as :toolTipComponent[credenciais de teste]{link="/developers/es/docs/qr-code/resources/credentials" linkText="Credenciais" content="Chaves de acesso únicas com as quais identificamos uma integração na sua conta, vinculadas à sua aplicação. Para mais informações, acesse o link abaixo."} serão criadas automaticamente. Você deverá utilizá-las para realizar todas as configurações e validações necessárias em um ambiente seguro de testes. Caso esteja utilizando uma aplicação já existente, será necessário [ativar as credenciais de teste](/developers/es/docs/qr-code/resources/credentials).

Ao acessar as credenciais de teste, serão exibidos os seguintes pares de credenciais: :toolTipComponent[Public Key]{content="Chave pública que é utilizada no _frontend_ para acessar informações e criptografar dados. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*."} e o :toolTipComponent[Access Token]{content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*."} de teste.

![credenciais de test](/images/qr-orders/credentials-test-panel-pt-v1.png) 

Para desenvolver sua integração com Código QR, utilize seu :toolTipComponent[Access Token de teste]{content="Chave privada da aplicação criada no Mercado Pago, utilizada no backend. Você pode acessá-la em *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante o processo de integração, utilize o Access Token de teste. Ao concluir a integração, substitua-o pelo Access Token de produção caso seja uma integração própria, ou pelo Access Token obtido via OAuth em integrações para terceiros."}. 

> WARNING
>
> Se você está integrando o Código QR em nome de um terceiro, recomenda-se utilizar o **Access Token de teste** durante todo o desenvolvimento e testes da integração. Antes de [subir em produção](/developers/pt/docs/qr-code/go-to-production), será necessário obter um Access Token de produção através do protocolo OAuth (fluxo [Authorization code](/developers/es/docs/security/oauth/creation#bookmark_authorization_code)) e substituí-lo.

Após obter as credenciais necessárias, continue para [criar a loja e caixa](/developers/pt/docs/qr-code/create-store-and-pos).

# Criar loja e caixa

Após [criar a aplicação](/developers/pt/docs/qr-code/create-application) e obter as credenciais, é necessário configurar a loja e caixa, que estarão associados às transações. 

As **lojas** representam estabelecimentos físicos cadastrados no Mercado Pago e podem ter um ou mais caixas vinculados. Já os **caixas** correspondem aos pontos de venda (PDVs) e devem sempre estar associados a uma loja, garantindo a conciliação de pagamentos por Código QR em estabelecimentos físicos. 

![Stores and POS](/images/qr-orders/stores_pos-v1.pt.png)

É possível criar lojas e caixas a partir do seu sistema através das nossas **APIs para pagamentos presenciais**. Para isso, siga os passos a seguir.

## Criar loja

Para criar uma loja via API, envie um **POST** incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no backend. Você pode acessá-la em *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante o processo de integração, utilize o Access Token de teste. Ao concluir a integração, substitua-o pelo Access Token de produção caso seja uma integração própria, ou pelo Access Token obtido via OAuth em integrações para terceiros."} ao endpoint :TagComponent{tag="API" text="Criar loja" href="/developers/pt/reference/in-person-payments/qr-code/stores/create-store/post"}. Você deverá adicionar o :toolTipComponent[`user_id` da conta de teste]{content="Durante o desenvolvimento da integração, utilize o User ID da sua conta de teste, disponível em *Suas integrações > Detalhes da aplicação > Credenciais de teste > Dados das credenciais de teste*. Ao subir em produção, substitua-o pelo User ID da conta real do Mercado Pago que receberá os pagamentos."} no _path_ da sua requisição e completar os parâmetros requeridos com os detalhes do negócio conforme se indica a seguir.

> WARNING
>
> É fundamental preencher corretamente todas as informações de localização da loja (`city_name`, `state_name`, `latitude` e `longitude`). Dados incorretos podem causar erros nos cálculos de impostos, impactando diretamente o faturamento e a regularização fiscal da sua empresa.

```curl
curl -X POST \
  'https://api.mercadopago.com/users/USER_ID/stores'\
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -d '{
  "name": "Loja Instore",
  "business_hours": {
  "monday": [
  {
  "open": "08:00",
  "close": "12:00"
  }
  ],
  "tuesday": [
  {
  "open": "09:00",
  "close": "18:00"
  }
  ]
  },
  "external_id": "LOJ001",
  "location": {
  "street_number": "0123",
  "street_name": "Nome da rua de exemplo.",
  "city_name": "Nome da cidade.",
  "state_name": "Nome do estado.",
  "latitude": 27.175193925922862,
  "longitude": 78.04213533235064,
  "reference": "Perto do Mercado Pago."
  }
}'
```

| Parâmetro | Descrição e exemplos | Obrigatoriedade |
| ---- | ---- | ---- |
| `user_id` | Identificador da conta do Mercado Pago que recebe o dinheiro pelas vendas realizadas na loja.<br><br>Durante o desenvolvimento, utilize o `user_id` da conta de teste, disponível em **Suas integrações > Detalhes da aplicação > Credenciais de teste > Dados das credenciais de teste**.<br><br>Ao subir em produção, substitua pelo `user_id` da conta real que receberá os pagamentos: Se você está realizando uma integração :toolTipComponent[própria]{content="Integrações de QR Code ao seu sistema para uso próprio e configuradas a partir das credenciais da sua aplicação."}, encontrará este valor nos [Detalhes da aplicação](/developers/pt/docs/qr-code/resources/application-details). Se, ao contrário, está realizando uma integração :toolTipComponent[para terceiros]{content="Integrações de QR Code ao seu sistema em nome de um vendedor e configuradas a partir de credenciais obtidas por meio do protocolo de segurança OAuth."}, obterá o valor na resposta à :toolTipComponent[vinculação por meio de OAuth]{link="/developers/pt/docs/qr-code/resources/security/landing-hub" linkText="OAuth" content="Chave privada gerada mediante o protocolo de segurança OAuth, que permite gerenciar integrações em nome de terceiros. Para mais informações, dirija-se à documentação."}. | Obrigatório |
| `name` | Nome da loja criada. | Obrigatório |
| `business_hours` | Horário comercial. Os horários de funcionamento são divididos por dia da semana e são permitidos até quatro horários de abertura e fechamento por dia. Informe esses dados para que sua loja seja exibida no aplicativo do Mercado Pago com o horário correto de funcionamento. | Opcional |
| `external_id` | Identificador externo da loja para o sistema integrador. Pode conter qualquer valor alfanumérico de até 60 caracteres e deve ser único para cada loja. Por exemplo, `LOJ001`. | Obligatorio |
| `location` | Este objeto deve conter todas as informações da localização da loja. É importante preencher tudo corretamente , especialmente os campos `latitude` e `longitude` com as coordenadas geográficas, usando o formato decimal simples e os dados reais do local. Por exemplo, `"latitude": 27.175193925922862` e `"longitude": 78.04213533235064`, que correspondem à localização exata do Taj Mahal, na Índia. Ao inserir esses dados corretamente, a loja aparecerá no mapa na localização indicada. | Obrigatório |

Se a solicitação foi enviada corretamente, a resposta será como o exemplo a seguir:

```json
{
  "id": 1234567,
  "name": "Loja Instore",
  "date_created": "2019-08-08T19:29:45.019Z",
  "business_hours": {
  "monday": [
  {
  "open": "08:00",
  "close": "12:00"
  }
  ],
  "tuesday": [
  {
  "open": "09:00",
  "close": "18:00"
  }
  ]
  },
  "location": {
  "address_line": "Nome da rua de exemplo, 0123, Nome da cidade, Nome do estado.",
  "latitude": 27.175193925922862,
  "longitude": 78.04213533235064,
  "reference": "Perto do Mercado Pago"
  },
  "external_id": "LOJ001"
}
```

Além dos dados enviados na solicitação, o endpoint retornará o **identificador atribuído à loja pelo Mercado Pago** sob o parâmetro `id`.

## Criar caixa

Para habilitar vendas com Mercado Pago, é indispensável que cada loja registrada tenha pelo menos um caixa vinculado. Para criar um caixa e associá-lo à loja previamente criada, envie um **POST** incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no backend. Você pode acessá-la em *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante o processo de integração, utilize o Access Token de teste. Ao concluir a integração, substitua-o pelo Access Token de produção caso seja uma integração própria, ou pelo Access Token obtido via OAuth em integrações para terceiros."} ao endpoint :TagComponent{tag="API" text="Criar caixa" href="/developers/pt/reference/in-person-payments/qr-code/pos/create-pos/post"} como mostrado a seguir.

```curl
curl -X POST \
  'https://api.mercadopago.com/pos'\
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -d '{
  "name": "First POS",
  "fixed_amount": true,
  "store_id": 1234567,
  "external_store_id": "LOJ001",
  "external_id": "LOJ001POS001",
  "category": 621102
}'
```

| Parâmetro | Descrição e exemplos | Obrigatoriedade |
| ---- | ---- | ---- |
| `name` | Nome do caixa criado. | Obrigatório |
| `fixed_amount` | Este campo determina se o cliente pode inserir o valor a pagar ou se já é predefinido pelo vendedor. Para modelos integrados, este valor deve ser igual a `true`. | Obrigatório |
| `store_id` | Identificador da loja à qual pertence o caixa, atribuído a essa loja pelo Mercado Pago. É retornado na resposta à criação da loja sob o parâmetro `id`. | Obrigatório |
| `external_store_id` | Identificador externo único da loja. Este valor é definido pelo integrador ao criar a loja, sob o parâmetro `external_id`. | Obrigatório |
| `external_id` | Identificador único do caixa definido pelo sistema integrador. Deve ser um valor alfanumérico único para cada caixa e pode conter até 40 caracteres. | Obrigatório |
| `category` | Código MCC que indica a categoria do ponto de venda. As únicas categorias possíveis são Gastronomia e Posto de gasolina, **e o código varia segundo o país de operação**. Se não for especificado, permanece como uma categoria genérica. Para mais informações sobre os códigos, consulte a :TagComponent{tag="API" text="Referência de API" href="/developers/pt/reference/in-person-payments/qr-code/pos/create-pos/post"}. | Opcional |

Se a solicitação foi enviada corretamente, a resposta será como o exemplo a seguir.

```json
{
  "id": 2711382,
  "qr": {
  "image": "https://www.mercadopago.com/instore/merchant/qr/2711382/0977011a027c4b4387e52069da4264deae2946af4dcc44ee98a8f1dbb376c8a1.png",
  "template_document": "https://www.mercadopago.com/instore/merchant/qr/2711382/template_0977011a027c4b4387e52069da4264deae2946af4dcc44ee98a8f1dbb376c8a1.pdf",
  "template_image": "https://www.mercadopago.com/instore/merchant/qr/2711382/template_0977011a027c4b4387e52069da4264deae2946af4dcc44ee98a8f1dbb376c8a1.png"
  },
  "status": "active",
  "date_created": "2019-08-22T14:11:12.000Z",
  "date_last_updated": "2019-08-25T15:16:12.000Z",
  "uuid": "0977011a027c4b4387e52069da4264deae2946af4dcc44ee98a8f1dbb376c8a1",
  "user_id": 446566691,
  "name": "First POS",
  "fixed_amount": false,
  "category": 621102,
  "store_id": 1234567,
  "external_store_id": "SUC001",
  "external_id": "SUC001POS001"
}
```

Veja na tabela abaixo a descrição de alguns dos parâmetros retornados que podem ser úteis para continuar com sua integração mais adiante.

| Parâmetro | Descrição |
| ---- | ---- |
| `id` | ID de criação do ponto de venda. Ao registrar um ponto de venda, você receberá um ID correspondente. Esse ID pode ser utilizado para várias operações, incluindo consultar seus dados. |
| `qr` | Código QR estático associado ao caixa criado automaticamente para processar as transações do ponto de venda. Este código QR é necessário quando as orders são criadas em modo estático (`static`) ou híbrido (`hybrid`). O objeto `qr` contém os seguintes atributos: <br>`image`: URL da imagem do código QR a ser utilizado para realizar as transações. <br>`template_document`: URL do arquivo (em formato PDF) do template com o código QR a ser utilizado para realizar as transações. <br>`template_image`: URL do arquivo (em formato de imagem) do template com o código QR a ser utilizado para processar as transações. |
| `status` | Status de criação do ponto de venda. |
| `uuid` | O UUID (*Universally Unique Identifier* - Identificador Universalmente Único) é um número de 128 bits utilizado para identificar informações. Neste caso, é o número de identificação do Código QR em questão. |
| `user_id` | Identificador da conta do Mercado Pago que recebe o dinheiro pelas vendas realizadas no caixa. |
| `name` | Nome atribuído ao caixa no momento da sua criação. |
| `store_id` | Identificador da loja à qual pertence o ponto de venda. |
| `external_store_id` | Identificador externo da loja, que foi atribuído pelo sistema integrador no momento da sua criação sob o parâmetro `external_id`. |
| `external_id` | Identificador único do caixa definido pelo sistema integrador. |

Se ambas as solicitações foram bem-sucedidas, você terá criado e configurado a loja e o caixa necessários para a integração com Código QR.

> NOTE
>
> As lojas são exibidas automaticamente no mapa das aplicações do Mercado Pago e Mercado Livre, ampliando a visibilidade do estabelecimento à medida que os pagamentos são processados.

Com a loja e o caixa criados, você poderá [integrar o processamento de pagamentos](/developers/pt/docs/qr-code-ca/payment-processing).

# Integrar o processamento de transações

O processamento de transações com código QR é realizado por meio da criação de orders que incluem transações de pagamento, seja com retiradas de dinheiro adicionais ou retiradas sem uma compra associada. Ao criar uma order, o cliente poderá realizar as transações de forma presencial escaneando o código.

Existem três modelos de Código QR disponíveis para integração, definidos no momento da criação da order:

* **Modelo estático**: Neste modelo, um único código QR associado ao caixa criado previamente recebe as informações de cada order gerada.
* **Modelo dinâmico**: Um código QR exclusivo e de pagamento único é gerado para cada transação, contendo os dados específicos da order criada.
* **Modelo híbrido**: Permite que a transação seja realizada tanto pelo QR estático quanto pelo dinâmico. A order é vinculada ao código QR estático do caixa, enquanto também é gerado um QR dinâmico simultaneamente. Uma vez que a transação seja realizada com qualquer um dos dois códigos, o outro ficará automaticamente desabilitado para uso.

Esta integração permite criar, processar e cancelar orders, além de realizar reembolsos e consultar informações e atualizações de status das transações de cash-out e extra-cash.

> WARNING 
>
> A funcionalidade de retirada de dinheiro está disponível apenas para clientes de Carteira Assessorada. Portanto, se você é vendedor e necessita dessa funcionalidade, é necessário solicitar a habilitação ao seu assessor comercial.

:::::AccordionComponent{title="Criar uma order com cash-out"}
Para configurar o processamento de transações de retirada de dinheiro com Código QR, é necessário identificar a loja e o caixa aos quais a order será associada. Lembre-se de que tanto a loja quanto o caixa devem ter sido [criados previamente](/developers/pt/docs/qr-code-ca/create-store-and-pos).

Em seguida, você poderá criar a order para cash-out. Para isso, envie uma solicitação **POST** ao endpoint :TagComponent{tag="API" text="/v1/orders" href="/developers/pt/reference/in-person-payments/qr-code-ca/orders/create-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code-ca/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Além disso, certifique-se de incluir o `external_pos_id` do caixa ao qual deseja atribuir a order, obtido na etapa anterior.

```curl
curl --location --request POST 'https://api.mercadopago.com/v1/orders' \
--header 'X-Idempotency-Key: 02ff8cd0-c4e9-4fe8-a977-6c3c2bc6336c' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}' \
--data '{
  "type": "qr",
  "transactions": {
  "cash_outs": [
  {
  "amount": "100.00",
  "additional_info": {
  "fixed_amount": true,
  "agency_type": "AGTEC",
  "bank_service_code": "abc123"
  }
  }
  ]
  },
  "external_reference": "ExtRef_123456",
  "config": {
  "qr": {
  "external_pos_id": "POSDOC",
  "mode": "static | dynamic | hybrid"
  }
  }
}'
```

| Parâmetro | Tipo | Descrição | Obrigatoriedade |
| ---- | ---- | ---- | ---- |
| `X-Idempotency-Key` | *header* | Chave de idempotência. Esta chave garante que cada solicitação seja processada apenas uma vez, evitando duplicidades. Utilize um valor exclusivo no `header` da solicitação, como um UUID (Universally Unique Identifier - Identificador Universalmente Único) V4 ou uma *string* aleatória. | Obrigatório |
| `type` | *string* | Tipo de order, associada à solução do Mercado Pago para a qual foi criada. Para transações com Código QR do Mercado Pago, o único valor possível é *qr*, que é o valor associado à criação de orders para transações com Código QR do Mercado Pago. | Obrigatório |
| `external_reference` | *string* | É a referência externa da order, atribuída no momento da criação. O limite máximo permitido é de 64 caracteres e os permitidos são: letras maiúsculas e minúsculas, números e os símbolos hífen (-) e sublinhado (_). O campo não pode ser utilizado para enviar dados PII. | Obrigatório |
| `config.qr.external_pos_id` | *string* | Identificador externo do caixa, definido pelo integrador durante sua criação. Ao incluí-lo, a informação da order fica associada ao caixa e à loja previamente criados dentro do sistema Mercado Pago. Importante: O campo `external_pos_id` deve ter o mesmo valor definido como `external_id` na criação do seu caixa. | Obrigatório |
| `config.qr.mode` | *string* | Modo de código QR associado à order. Os valores possíveis estão listados abaixo e, se nenhum for enviado, o valor padrão será `static`. <br> `static`: Modo estático, em que o código QR estático associado ao caixa definido no campo `external_pos_id` recebe a informação da order. <br>`dynamic`: Modo dinâmico, em que um código QR único é gerado para cada transação, incluindo os dados específicos da order criada. Este código deve ser construído a partir da informação retornada no campo `qr_data` da resposta, cujo valor é exclusivo para cada order. <br>`hybrid`: Permite que a transação seja realizada usando qualquer um dos dois modos, estático ou dinâmico, já que a order será vinculada ao código QR estático associado ao caixa (`external_pos_id`), e um QR será gerado dinamicamente em paralelo. No entanto, apenas um dos QR gerados poderá ser utilizado pelo cliente. | Opcional |
| `transactions.cash_outs` | *array* | Array com informações sobre as transações de retirada de dinheiro associadas à order. | Obrigatório |
| `transactions.cash_outs.amount` | *string* | Valor da retirada. Pode conter dois decimais ou nenhum. | Obrigatório |
| `transactions.cash_outs.additional_info` | *object* | Campo válido apenas para Brasil (BRA). Especifica informações adicionais de retiradas de dinheiro. | Condicional |
| `transactions.cash_outs.additional_info.fixed_amount` | *boolean* | Campo válido apenas para Brasil (BRA). Determina se o pagador pode definir o valor da retirada de sua carteira do Mercado Pago. Se definido como `true`, o pagador poderá definir o valor, e se definido como `false`, ele será definido pelo vendedor e não poderá ser modificado. | Condicional |
| `transactions.cash_outs.additional_info.agency_type` | *string* | Campo válido apenas para Brasil (BRA). Tipo de agência ou entidade financeira que está processando a transação. Valores possíveis: `AGTEC` (Estabelecimentos comerciais), `AGTOT` (Agentes cuja atividade é a prestação de serviços auxiliares), `AGPSS` (Participante que facilita a retirada de dinheiro). | Condicional |
| `transactions.cash_outs.additional_info.bank_service_code` | *string* | Campo válido apenas para Brasil (BRA). Identifica o tipo de serviço bancário solicitado por meio da transação. Este campo pode assumir qualquer valor. | Condicional |

> NOTE
>
> Para mais detalhes sobre os parâmetros que devem ser enviados nesta solicitação, consulte nossa [Referência de API](/developers/pt/reference/in-person-payments/qr-code-ca/orders/create-order/post).

A resposta varia conforme o modelo de QR escolhido para a integração. Selecione abaixo a opção que corresponde ao seu caso.

::::TabsComponent

:::TabComponent{title="Modelo estático"}
Ao criar uma order especificando o campo `config.qr.mode` como `static`, o QR que deverá ser escaneado pelo cliente é **o obtido na resposta à solicitação de criação da caixa**, pois é esse que receberá as informações da order criada. Se a solicitação for bem-sucedida, a resposta retornará uma order com status `created`.

Consulte abaixo um exemplo de resposta para uma solicitação de criação de uma order para **retirada de dinheiro (cash-out)** no modelo estático.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code-ca/test-integration).

```json
{
  "id": "ORD01JYHP5MGKC5PMPZBHSTMLNDQX",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ExtRef_123456",
  "total_amount": "100.00",
  "expiration_time": "PT15M",
  "country_code": "BRA",
  "user_id": "1898180000",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-06-24T19:20:52.429Z",
  "last_updated_date": "2025-06-24T19:20:52.429Z",
  "integration_data": {
  "application_id": "8950412930770000"
  },
  "transactions": {
  "cash_outs": [
  {
  "id": "CAS01JYHP5MGKC5PMPZBHSW42LLPA",
  "amount": "100.00",
  "status": "created",
  "status_detail": "ready_to_process"
  }
  ]
  },
  "config": {
  "qr": {
  "external_pos_id": "POSDOC",
  "mode": "static"
  }
  }
}
```

A order criada será automaticamente vinculada ao caixa especificado na solicitação, permitindo que o cliente realize a transação no ponto de venda físico. Além disso, a vinculação também facilita a conciliação. Após a transação, ela será processada de forma integrada.

:::

:::TabComponent{title="Modelo dinâmico"}
Ao criar uma order especificando o modo `dynamic` no campo `config.qr.mode`, a resposta da solicitação incluirá o campo adicional `type_response.qr_data`. Este campo contém uma *string* no formato [EMVCo](https://www.emvco.com/emv-technologies/qr-codes/), que pode ser convertida em um código QR para ser impresso ou exibido em uma tela ou dispositivo. Se a solicitação for bem-sucedida, a resposta retornará uma order com status `created`.

Consulte abaixo um exemplo de resposta para uma solicitação de criação de uma order para **retirada de dinheiro (cash-out)** no modelo dinâmico.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code-ca/test-integration).

```json
{
  "id": "ORD01JYHP5MGKC5PMPZBHSTMLNDQX",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ExtRef_123456",
  "total_amount": "100.00",
  "expiration_time": "PT15M",
  "country_code": "BRA",
  "user_id": "1898180000",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-06-24T19:20:52.429Z",
  "last_updated_date": "2025-06-24T19:20:52.429Z",
  "integration_data": {
  "application_id": "8950412930770000"
  },
  "transactions": {
  "cash_outs": [
  {
  "id": "CAS01JYHP5MGKC5PMPZBHSW42LLPA",
  "amount": "100.00",
  "status": "created",
  "status_detail": "ready_to_process"
  }
  ]
  },
  "config": {
  "qr": {
  "external_pos_id": "POSDOC",
  "mode": "dynamic"
  }
  },
  "type_response": {
  "qr_data": "00020101021226580014br.gov.bcb.qr01368ee55a9c-7db3-41e0-a8cd-fbff4d4765b5204000053039865802BR5925PABLO JOSE DE OLIVEIRA CA6009SAO PAULO61088051040062070503***630442E4"
  }
}
```

Neste modelo, um código QR exclusivo é gerado para cada order criada, incorporando os dados específicos da transação. Após a transação, ela é processada de forma integrada.

:::

:::TabComponent{title="Modelo híbrido"}
Ao criar uma order especificando o modo `hybrid` no campo `config.qr.mode`, a resposta da solicitação inclui o campo adicional `type_response.qr_data`. Assim como no modelo dinâmico, o valor deste campo contém uma *string* no formato [EMVCo](https://www.emvco.com/emv-technologies/qr-codes/), que pode ser convertida em um código QR para uso pelo cliente.

Além disso, o cliente também poderá escanear o código QR obtido na resposta da solicitação de criação da caixa para realizar a transação, como ocorre no modelo estático, pois é esse que receberá as informações da order criada.

Dessa forma, a transação pode ser feita tanto pelo **QR estático do caixa** quanto por um **QR dinâmico** gerado ao mesmo tempo. A order é sempre vinculada ao QR estático, mas o cliente pode optar por usar qualquer um dos dois. Assim que a transação é concluída em um deles, o outro é automaticamente desabilitado, evitando duplicidade de transações.

Se a solicitação for bem-sucedida, a resposta retornará uma order com `status created`. Veja abaixo um exemplo de resposta para uma solicitação de criação de uma order para **retirada de dinheiro (cash-out)** no modelo híbrido.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code-ca/test-integration).

```json
{
  "id": "ORD01JYHP5MGKC5PMPZBHSTMLNDQX",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ExtRef_123456",
  "total_amount": "100.00",
  "expiration_time": "PT15M",
  "country_code": "BRA",
  "user_id": "1898180000",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-06-24T19:20:52.429Z",
  "last_updated_date": "2025-06-24T19:20:52.429Z",
  "integration_data": {
  "application_id": "8950412930770000"
  },
  "transactions": {
  "cash_outs": [
  {
  "id": "CAS01JYHP5MGKC5PMPZBHSW42LLPA",
  "amount": "100.00",
  "status": "created",
  "status_detail": "ready_to_process"
  }
  ]
  },
  "config": {
  "qr": {
  "external_pos_id": "POSDOC",
  "mode": "hybrid"
  }
  },
  "type_response": {
  "qr_data": "00020101021226580014br.gov.bcb.qr01368ee55a9c-7db3-41e0-a8cd-fbff4d4765b5204000053039865802BR5925PABLO JOSE DE OLIVEIRA CA6009SAO PAULO61088051040062070503***630442E4"
  }
}
```

:::

::::

> WARNING
>
> Armazene o `id` da order retornado na criação. Ele é necessário para operações futuras e para validar notificações. Consulte **Recursos** para mais [detalhes sobre status da order e transação](/developers/pt/docs/qr-code-ca/resources/status-order-transaction).

:::::

:::::AccordionComponent{title="Criar uma order com pagamento"}
Para configurar o processamento de pagamentos com Código QR, é necessário identificar a loja e o caixa aos quais a order será associada. Lembre-se de que tanto a loja quanto o caixa devem ter sido [criados previamente](/developers/pt/docs/qr-code-ca/create-store-and-pos).

Em seguida, você poderá criar a order para pagamento. Para isso, envie uma solicitação **POST** ao endpoint :TagComponent{tag="API" text="/v1/orders" href="/developers/pt/reference/in-person-payments/qr-code-ca/orders/create-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code-ca/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Além disso, certifique-se de incluir o `external_pos_id` do caixa ao qual deseja atribuir a order, obtido na etapa anterior.

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

| Parâmetro | Tipo | Descrição | Obrigatoriedade |
| ---- | ---- | ---- | ---- |
| `X-Idempotency-Key` | *header* | Chave de idempotência. Esta chave garante que cada solicitação seja processada apenas uma vez, evitando duplicidades. Utilize um valor exclusivo no `header` da solicitação, como um UUID (Universally Unique Identifier - Identificador Universalmente Único) V4 ou uma *string* aleatória. | Obrigatório |
| `type` | *string* | Tipo de order, associada à solução do Mercado Pago para a qual foi criada. Para transações com Código QR do Mercado Pago, o único valor possível é *qr*, que é o valor associado à criação de orders para transações com Código QR do Mercado Pago. | Obrigatório |
| `total_amount` | *string* | Valor total da order. Representa a soma das transações. Pode conter dois decimais ou nenhum. Exemplo: 50.00. | Opcional |
| `description` | *string* | Descrição do produto ou serviço. O limite máximo é de 150 caracteres e não pode ser utilizada para enviar dados PII. | Opcional |
| `external_reference` | *string* | É a referência externa da order, atribuída no momento da criação. O limite máximo permitido é de 64 caracteres e os permitidos são: letras maiúsculas e minúsculas, números e os símbolos hífen (-) e sublinhado (_). O campo não pode ser utilizado para enviar dados PII. | Obrigatório |
| `config.qr.external_pos_id` | *string* | Identificador externo do caixa, definido pelo integrador durante sua criação. Ao incluí-lo, a informação da order fica associada ao caixa e à loja previamente criados dentro do sistema Mercado Pago. Importante: O campo `external_pos_id` deve ter o mesmo valor definido como `external_id` na criação do seu caixa. | Obrigatório |
| `config.qr.mode` | *string* | Modo de código QR associado à order. Os valores possíveis estão listados abaixo e, se nenhum for enviado, o valor padrão será `static`. <br> `static`: Modo estático, em que o código QR estático associado ao caixa definido no campo `external_pos_id` recebe a informação da order. <br>`dynamic`: Modo dinâmico, em que um código QR único é gerado para cada transação, incluindo os dados específicos da order criada. Este código deve ser construído a partir da informação retornada no campo `qr_data` da resposta, cujo valor é exclusivo para cada order. <br>`hybrid`: Permite que a transação seja realizada usando qualquer um dos dois modos, estático ou dinâmico, já que a order será vinculada ao código QR estático associado ao caixa (`external_pos_id`), e um QR será gerado dinamicamente em paralelo. No entanto, apenas um dos QR gerados poderá ser utilizado pelo cliente. | Opcional |
| `transactions.payments` | *array* | Array com informações sobre as transações de pagamento associadas à order. | Obrigatório |
| `transactions.payments.amount` | *string* | Valor do pagamento. Pode conter dois decimais ou nenhum. Exemplo: 50.00. | Obrigatório |

> NOTE
>
> Para mais detalhes sobre os parâmetros que devem ser enviados nesta solicitação, consulte nossa [Referência de API](/developers/pt/reference/in-person-payments/qr-code-ca/orders/create-order/post).

A resposta varia conforme o modelo de QR escolhido para a integração. Selecione abaixo a opção que corresponde ao seu caso.

::::TabsComponent

:::TabComponent{title="Modelo estático"}
Ao criar uma order especificando o campo `config.qr.mode` como `static`, o QR que deverá ser escaneado pelo cliente é **o obtido na resposta à solicitação de criação da caixa**, pois é esse que receberá as informações da order criada. Se a solicitação for bem-sucedida, a resposta retornará uma order com status `created`.

Confira abaixo um exemplo de resposta para uma solicitação de criação de uma order para **pagamentos** no modelo estático.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code-ca/test-integration).

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
  "user_id": "240424235",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-08-21T19:32:21.621Z",
  "last_updated_date": "2025-08-21T19:32:21.621Z",
  "integration_data": {
  "application_id": "147632494144930"
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

A order criada será automaticamente vinculada ao caixa especificado na solicitação, permitindo que o comprador realize o pagamento no ponto de venda físico. Além disso, a vinculação também facilita a conciliação. Após o pagamento, a transação será processada de forma integrada.

:::

:::TabComponent{title="Modelo dinâmico"}
Ao criar uma order especificando o modo `dynamic` no campo `config.qr.mode`, a resposta da solicitação incluirá o campo adicional `type_response.qr_data`. Este campo contém uma *string* no formato [EMVCo](https://www.emvco.com/emv-technologies/qr-codes/), que pode ser convertida em um código QR para ser impresso ou exibido em uma tela ou dispositivo. Se a solicitação for bem-sucedida, a resposta retornará uma order com status `created`.

Confira abaixo um exemplo de resposta para uma solicitação de criação de uma order para **pagamentos** no modelo dinâmico.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code-ca/test-integration).

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
  "user_id": "240424235",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-08-21T19:43:10.13Z",
  "last_updated_date": "2025-08-21T19:43:10.13Z",
  "integration_data": {
  "application_id": "147632494144930"
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

Neste modelo, um código QR exclusivo é gerado para cada order criada, incorporando os dados específicos da transação. Após o pagamento, a transação é processada de forma integrada.

:::

:::TabComponent{title="Modelo híbrido"}
Ao criar uma order especificando o modo `hybrid` no campo `config.qr.mode`, a resposta da solicitação inclui o campo adicional `type_response.qr_data`. Assim como no modelo dinâmico, o valor deste campo contém uma *string* no formato [EMVCo](https://www.emvco.com/emv-technologies/qr-codes/), que pode ser convertida em um código QR para impressão e pagamento pelo cliente.

Além disso, o cliente também poderá escanear o código QR obtido na resposta da solicitação de criação da caixa para realizar o pagamento, como ocorre no modelo estático, pois é esse que receberá as informações da order criada.

Dessa forma, o pagamento pode ser feito tanto pelo **QR estático do caixa** quanto por um **QR dinâmico** gerado ao mesmo tempo. A order é sempre vinculada ao QR estático, mas o cliente pode optar por usar qualquer um dos dois. Assim que o pagamento é concluído em um deles, o outro é automaticamente desabilitado, evitando duplicidade de transações.

Se a solicitação for bem-sucedida, a resposta retornará uma order com `status created`. Veja abaixo um exemplo de resposta para uma solicitação de criação de uma order para **pagamentos** no modelo híbrido.

> NOTE
>
> Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação [Testar a integração](/developers/pt/docs/qr-code-ca/test-integration).

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
  "user_id": "240424235",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-08-21T20:21:43.987Z",
  "last_updated_date": "2025-08-21T20:21:43.987Z",
  "integration_data": {
  "application_id": "147632494144930"
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

:::

::::

> WARNING
>
> Armazene o `id` da order e o `id` do pagamento (`transactions.payments.id`) retornados na criação. Eles são necessários para operações futuras e para validar notificações. Consulte **Recursos** para mais [detalhes sobre status da order e transação](/developers/pt/docs/qr-code-ca/resources/status-order-transaction).

:::::

:::AccordionComponent{title="Criar uma order com extra-cash"}
Para configurar o processamento de transações combinadas (pagamento + retirada) com Código QR, é necessário identificar a loja e o caixa aos quais a order será associada. Lembre-se de que tanto a loja quanto o caixa devem ter sido [criados previamente](/developers/pt/docs/qr-code-ca/create-store-and-pos).

Em seguida, você poderá criar a order extra-cash. Para isso, envie uma solicitação **POST** ao endpoint :TagComponent{tag="API" text="/v1/orders" href="/developers/pt/reference/in-person-payments/qr-code-ca/orders/create-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code-ca/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Além disso, certifique-se de incluir o `external_pos_id` do caixa ao qual deseja atribuir a order, obtido na etapa anterior.

> WARNING
>
> Quando a order criada contém retiradas de dinheiro e pagamentos, o valor do campo `discounts.payment_methods.new_total_amount` deve ser a soma do valor do `cash_out` mais o valor do `payment` com o desconto aplicado, e não pode ser menor ou igual ao valor do `cash_out`.

Para criar uma order extra-cash, você deve incluir ambas as transações (`cash_outs` e `payments`) na solicitação, conforme o seguinte exemplo:

```curl
curl --location --request POST 'https://api.mercadopago.com/v1/orders' \
--header 'X-Idempotency-Key: 02ff8cd0-c4e9-4fe8-a977-6c3c2bc6336c' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}' \
--data '{
  "type": "qr",
  "total_amount": "140.00",
  "transactions": {
  "cash_outs": [
  {
  "amount": "110.00"
  }
  ],
  "payments": [
  {
  "amount": "30.00"
  }
  ]
  },
  "external_reference": "ExtRef_123456",
  "config": {
  "qr": {
  "external_pos_id": "POSDOC",
  "mode": "static"
  }
  },
  "description": "Description test",
  "items": [
  {
  "title": "Item test",
  "unit_price": "30.00",
  "quantity": 1,
  "unit_measure": "unit",
  "external_code": "1234567"
  }
  ]
}'
```

| Parâmetro | Tipo | Descrição | Obrigatoriedade |
| ---- | ---- | ---- | ---- |
| `X-Idempotency-Key` | *header* | Chave de idempotência. Esta chave garante que cada solicitação seja processada apenas uma vez, evitando duplicidades. Utilize um valor exclusivo no `header` da solicitação, como um UUID (Universally Unique Identifier - Identificador Universalmente Único) V4 ou uma *string* aleatória. | Obrigatório |
| `type` | *string* | Tipo de order, associada à solução do Mercado Pago para a qual foi criada. Para transações com Código QR do Mercado Pago, o único valor possível é *qr*, que é o valor associado à criação de orders para transações com Código QR do Mercado Pago. | Obrigatório |
| `total_amount` | *string* | Valor total da order. Representa a soma das transações, portanto, deve ser a soma do valor do `cash_out` mais o valor do `payment`. Pode conter dois decimais ou nenhum. Exemplo: 140.00. | Obrigatório |
| `external_reference` | *string* | É a referência externa da order, atribuída no momento da criação. O limite máximo permitido é de 64 caracteres e os permitidos são: letras maiúsculas e minúsculas, números e os símbolos hífen (-) e sublinhado (_). O campo não pode ser utilizado para enviar dados PII. | Obrigatório |
| `config.qr.external_pos_id` | *string* | Identificador externo do caixa, definido pelo integrador durante sua criação. Ao incluí-lo, a informação da order fica associada ao caixa e à loja previamente criados dentro do sistema Mercado Pago. Importante: O campo `external_pos_id` deve ter o mesmo valor definido como `external_id` na criação do seu caixa. | Obrigatório |
| `config.qr.mode` | *string* | Modo de código QR associado à order. Os valores possíveis estão listados abaixo e, se nenhum for enviado, o valor padrão será `static`. <br> `static`: Modo estático, em que o código QR estático associado ao caixa definido no campo `external_pos_id` recebe a informação da order. <br>`dynamic`: Modo dinâmico, em que um código QR único é gerado para cada transação, incluindo os dados específicos da order criada. Este código deve ser construído a partir da informação retornada no campo `qr_data` da resposta, cujo valor é exclusivo para cada order. <br>`hybrid`: Permite que a transação seja realizada usando qualquer um dos dois modos, estático ou dinâmico, já que a order será vinculada ao código QR estático associado ao caixa (`external_pos_id`), e um QR será gerado dinamicamente em paralelo. No entanto, apenas um dos QR gerados poderá ser utilizado pelo cliente. | Opcional |
| `transactions.cash_outs` | *array* | Array com informações sobre as transações de retirada de dinheiro associadas à order. | Obrigatório |
| `transactions.cash_outs.amount` | *string* | Valor da retirada. Pode conter dois decimais ou nenhum. Exemplo: 110.00. | Obrigatório |
| `transactions.payments` | *array* | Array com informações sobre as transações de pagamento associadas à order. | Obrigatório |
| `transactions.payments.amount` | *string* | Valor do pagamento. Pode conter dois decimais ou nenhum. Exemplo: 30.00. | Obrigatório |
| `description` | *string* | Descrição do produto ou serviço. O limite máximo é de 150 caracteres e não pode ser utilizada para enviar dados PII. | Opcional |
| `items` | *array* | Array com informações sobre os itens da order. | Opcional |

> NOTE
>
> Para mais detalhes sobre os parâmetros que devem ser enviados nesta solicitação, consulte nossa [Referência de API](/developers/pt/reference/in-person-payments/qr-code-ca/orders/create-order/post).

Consulte abaixo um exemplo de resposta para uma solicitação de criação de uma order **extra-cash** (combinando pagamento e retirada).

```json
{
  "id": "ORD01JYHP5MGKC5PMPZBHSTMLNDQX",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ExtRef_123456",
  "total_amount": "140.00",
  "expiration_time": "PT15M",
  "country_code": "BRA",
  "user_id": "1898180000",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-06-24T19:20:52.429Z",
  "last_updated_date": "2025-06-24T19:20:52.429Z",
  "integration_data": {
  "application_id": "8950412930770000"
  },
  "transactions": {
  "cash_outs": [
  {
  "id": "CAS01JYHP5MGKC5PMPZBHSW42LLPA",
  "amount": "110.00",
  "status": "created",
  "status_detail": "ready_to_process"
  }
  ],
  "payments": [
  {
  "id": "PAY01JYHP5MGKC5PMPZBHSW42LLPA",
  "amount": "30.00",
  "status": "created",
  "status_detail": "ready_to_process"
  }
  ]
  },
  "config": {
  "qr": {
  "external_pos_id": "POSDOC",
  "mode": "static"
  }
  },
  "description": "Description test",
  "items": [
  {
  "title": "Item test",
  "unit_price": "30.00",
  "quantity": 1,
  "unit_measure": "unit",
  "external_code": "1234567"
  }
  ]
}
```

Armazene o `id` da order retornado na criação. Ele é necessário para operações futuras e para validar notificações. Consulte **Recursos** para mais [detalhes sobre status da order e transação](/developers/pt/docs/qr-code-ca/resources/status-order-transaction).

:::

:::AccordionComponent{title="Cancelar order"}
O cancelamento de uma order só pode ser realizado quando seu `status` for `created`. Se a solicitação de cancelamento for feita com outro status, a API retornará um erro informando o conflito.

Para cancelar uma order, envie um **POST** para o endpoint :TagComponent{tag="API" text="/v1/orders/{order_id}/cancel" href="/developers/pt/reference/in-person-payments/qr-code-ca/orders/cancel-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code-ca/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Também é necessário enviar o `id` da order que deseja cancelar, obtido na resposta à sua criação.

```curl
curl --location --request POST 'https://api.mercadopago.com/v1/orders/ORD01JYHP5MGKC5PMPZBHSTMLNDQX/cancel' \
--header 'X-Idempotency-Key: 42979d9d-ef66-453b-89b7-1d6f3efc6597' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Se a solicitação for bem-sucedida, a resposta incluirá o campo `status` com o valor `canceled`.

```json
{
  "id": "ORD01JYHV71X1XAE89DGDBJ93AYAM",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ER_123456",
  "total_amount": "100.00",
  "expiration_time": "PT15M",
  "country_code": "URY",
  "user_id": "1898180608",
  "status": "canceled",
  "status_detail": "canceled",
  "currency": "UYU",
  "created_date": "2025-06-24T20:49:01.638Z",
  "last_updated_date": "2025-06-24T20:49:10.303Z",
  "integration_data": {
  "application_id": "8950412930771472"
  },
  "transactions": {
  "cash_outs": [
  {
  "id": "CAS01JYHV71X1XAE89DGDBMXPJQD6",
  "amount": "100.00",
  "status": "canceled",
  "status_detail": "canceled_by_api"
  }
  ]
  },
  "config": {
  "qr": {
  "external_pos_id": "SUC001POS001",
  "mode": "static"
  }
  }
}
```
:::

:::AccordionComponent{title="Reembolsar uma order"}

É possível reembolsar uma order criada por meio da nossa API. Neste caso, o reembolso será sempre uma devolução total do valor da order. Para efetuar o reembolso de uma order via API, ela deve estar com o `status processed`. Se o status for diferente, a API retornará uma mensagem de erro indicando o conflito.

> WARNING
>
> Uma order poderá ser reembolsada via API até **180 dias após a realização da transação**. Após esse período, não será possível efetuar a devolução.

Para realizar o reembolso total de uma order, envie um **POST** para o endpoint :TagComponent{tag="API" text="/v1/orders/{order_id}/refund" href="/developers/pt/reference/in-person-payments/qr-code-ca/orders/refund-order/post"}, incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code-ca/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Também é necessário informar o `id` da order que deseja reembolsar, obtido na resposta à sua criação.

```curl
curl --location --request POST 'https://api.mercadopago.com/v1/orders/ORDER_ID/refund' \
--header 'X-Idempotency-Key: 91b59be9-27b8-449f-a6bd-32dca8b424cd' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Se a solicitação for bem-sucedida, a resposta trará o `status processed` e um novo nó `transactions.refunds`, que conterá os detalhes do reembolso, além do `id` da transação original e o `id` da transação de reembolso.

```json
{
  "id": "ORD01JYHREYXTR31HRB5S9Q9G8QS7",
  "status": "processed",
  "status_detail": "accredited",
  "transactions": {
  "refunds": [
  {
  "id": "REF01JYHRNEK69845P0E6VBXKXAKK",
  "transaction_id": "CAS01JYHREYXTR31HRB5S9QE68G0N",
  "reference_id": "116228240060",
  "amount": "100.00",
  "status": "processing"
  }
  ]
  }
}

```

Na resposta da solicitação de reembolso, é criada uma nova transação do tipo `refund` com `status processing`. Para acompanhar o status final do reembolso, aguarde a notificação de atualização ou consulte os dados da order para verificar seu status. Quando o reembolso for confirmado, o status será alterado para `refunded`.

Após a confirmação do estorno, o status da order pode ser consultado através da requisição GET, conforme o exemplo abaixo:

```json
{
  "id": "ORD01JYHREYXTR31HRB5S9Q9G8QS7",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ExtRef_123456",
  "total_amount": "100.00",
  "expiration_time": "PT15M",
  "country_code": "URY",
  "user_id": "1898180608",
  "status": "refunded",
  "status_detail": "refunded",
  "created_date": "2025-06-24T20:00:55.137Z",
  "last_updated_date": "2025-06-24T20:04:27.622Z",
  "integration_data": {
  "application_id": "8950412930771472"
  },
  "transactions": {
  "cash_outs": [
  {
  "id": "CAS01JYHREYXTR31HRB5S9QE68G0N",
  "reference_id": "116228240060",
  "amount": "100.00",
  "status": "refunded",
  "status_detail": "refunded"
  }
  ],
  "refunds": [
  {
  "id": "REF01JYHRNEK69845P0E6VBXKXAKK",
  "transaction_id": "CAS01JYHREYXTR31HRB5S9QE68G0N",
  "reference_id": "116228240060",
  "amount": "100.00",
  "status": "processed"
  }
  ]
  },
  "config": {
  "qr": {
  "external_pos_id": "SUC001POS001",
  "mode": "static"
  }
  }
}
```

O campo `transaction_id` no refund identifica qual transação (`payments` ou `cash_outs`) está sendo reembolsada. Se identificar uma transação de pagamento, o valor começará com o prefixo `PAY`, e se identificar uma transação de retirada, o valor começará com o prefixo `CAS`.

:::

:::AccordionComponent{title="Consultar dados de uma order"}
É possível consultar os dados de uma order e suas transações associadas, sejam pagamentos, retiradas ou reembolsos, incluindo seus status ou valores.

Para realizar a consulta, envie um **GET** ao endpoint :TagComponent{tag="API" text="/v1/orders/{order_id}" href="/developers/pt/reference/in-person-payments/qr-code-ca/orders/get-order/get"} incluindo seu :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/qr-code-ca/create-application" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, que é utilizada no backend. Você pode acessá-la através de *Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste*. Durante a integração, utilize o Access Token de teste e, ao finalizar, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros. Para mais informações, acesse a documentação."}. Além disso, certifique-se de incluir o `id` da order obtido na resposta à sua criação.

```curl
curl --location --request GET 'https://api.mercadopago.com/v1/orders/ORDER_ID' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

> WARNING
>
> Esta solicitação está disponível apenas para orders criadas há menos de 3 meses. Para acessar informações de orders mais antigas, é necessário contatar nosso atendimento ao cliente. 

Se a solicitação for bem-sucedida, a resposta retornará toda a informação da order, incluindo seu status, o status das transações e/ou o status do reembolso em tempo real.

```json
{
  "id": "ORD01JYHP5MGKC5PMPZBHSTMLNDQX",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ExtRef_123456",
  "total_amount": "100.00",
  "expiration_time": "PT15M",
  "country_code": "BRA",
  "user_id": "1898180608",
  "status": "created",
  "status_detail": "created",
  "created_date": "2025-06-24T19:46:02.381Z",
  "last_updated_date": "2025-06-24T19:46:02.381Z",
  "integration_data": {
  "application_id": "8950412930771472"
  },
  "transactions": {
  "cash_outs": [
  {
  "id": "CAS01JYHQKQ2D39PCBEK3K36G0SQD",
  "amount": "100.00",
  "status": "created",
  "status_detail": "ready_to_process"
  }
  ]
  },
  "config": {
  "qr": {
  "external_pos_id": "SUC001POS001",
  "mode": "static"
  }
  }
}
```

:::

Após a integração do processamento de transações, você poderá [configurar as notificações](/developers/pt/docs/qr-code-ca/notifications).

Criar uma order com pagamento
Para configurar o processamento de pagamentos com Código QR, é necessário identificar a loja e o caixa aos quais a order será associada. Lembre-se de que tanto a loja quanto o caixa devem ter sido criados previamente.

Em seguida, você poderá criar a order para pagamento. Para isso, envie uma solicitação POST ao endpoint /v1/ordersAPI, incluindo seu Access Token de teste
. Além disso, certifique-se de incluir o external_pos_id do caixa ao qual deseja atribuir a order, obtido na etapa anterior.

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
Parâmetro	Tipo	Descrição	Obrigatoriedade
X-Idempotency-Key	header	Chave de idempotência. Esta chave garante que cada solicitação seja processada apenas uma vez, evitando duplicidades. Utilize um valor exclusivo no header da solicitação, como um UUID (Universally Unique Identifier - Identificador Universalmente Único) V4 ou uma string aleatória.	Obrigatório
type	string	Tipo de order, associada à solução do Mercado Pago para a qual foi criada. Para transações com Código QR do Mercado Pago, o único valor possível é qr, que é o valor associado à criação de orders para transações com Código QR do Mercado Pago.	Obrigatório
total_amount	string	Valor total da order. Representa a soma das transações. Pode conter dois decimais ou nenhum. Exemplo: 50.00.	Opcional
description	string	Descrição do produto ou serviço. O limite máximo é de 150 caracteres e não pode ser utilizada para enviar dados PII.	Opcional
external_reference	string	É a referência externa da order, atribuída no momento da criação. O limite máximo permitido é de 64 caracteres e os permitidos são: letras maiúsculas e minúsculas, números e os símbolos hífen (-) e sublinhado (_). O campo não pode ser utilizado para enviar dados PII.	Obrigatório
config.qr.external_pos_id	string	Identificador externo do caixa, definido pelo integrador durante sua criação. Ao incluí-lo, a informação da order fica associada ao caixa e à loja previamente criados dentro do sistema Mercado Pago. Importante: O campo external_pos_id deve ter o mesmo valor definido como external_id na criação do seu caixa.	Obrigatório
config.qr.mode	string	Modo de código QR associado à order. Os valores possíveis estão listados abaixo e, se nenhum for enviado, o valor padrão será static.
static: Modo estático, em que o código QR estático associado ao caixa definido no campo external_pos_id recebe a informação da order.
dynamic: Modo dinâmico, em que um código QR único é gerado para cada transação, incluindo os dados específicos da order criada. Este código deve ser construído a partir da informação retornada no campo qr_data da resposta, cujo valor é exclusivo para cada order.
hybrid: Permite que a transação seja realizada usando qualquer um dos dois modos, estático ou dinâmico, já que a order será vinculada ao código QR estático associado ao caixa (external_pos_id), e um QR será gerado dinamicamente em paralelo. No entanto, apenas um dos QR gerados poderá ser utilizado pelo cliente.	Opcional
transactions.payments	array	Array com informações sobre as transações de pagamento associadas à order.	Obrigatório
transactions.payments.amount	string	Valor do pagamento. Pode conter dois decimais ou nenhum. Exemplo: 50.00.	Obrigatório
Para mais detalhes sobre os parâmetros que devem ser enviados nesta solicitação, consulte nossa Referência de API.
A resposta varia conforme o modelo de QR escolhido para a integração. Selecione abaixo a opção que corresponde ao seu caso.

Ao criar uma order especificando o modo dynamic no campo config.qr.mode, a resposta da solicitação incluirá o campo adicional type_response.qr_data. Este campo contém uma string no formato EMVCo, que pode ser convertida em um código QR para ser impresso ou exibido em uma tela ou dispositivo. Se a solicitação for bem-sucedida, a resposta retornará uma order com status created.

Confira abaixo um exemplo de resposta para uma solicitação de criação de uma order para pagamentos no modelo dinâmico.

Durante o desenvolvimento da integração, é possível escanear os códigos QR gerados utilizando o aplicativo do Mercado Pago, acessando-o com uma conta de teste de comprador. Para mais informações, consulte a documentação Testar a integração.
{
  "id": "ORD01K372G4J4FXZ9HGHZMJMGGPKE",
  "type": "qr",
  "processing_mode": "automatic",
  "external_reference": "ext_ref_1234",
  "description": "Smartphone",
  "total_amount": "50.00",
  "expiration_time": "PT16M",
  "country_code": "BRA",
  "user_id": "240424235",
  "status": "created",
  "status_detail": "created",
  "currency": "BRL",
  "created_date": "2025-08-21T19:43:10.13Z",
  "last_updated_date": "2025-08-21T19:43:10.13Z",
  "integration_data": {
    "application_id": "147632494144930"
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
Neste modelo, um código QR exclusivo é gerado para cada order criada, incorporando os dados específicos da transação. Após o pagamento, a transação é processada de forma integrada.

Armazene o id da order e o id do pagamento (transactions.payments.id) retornados na criação. Eles são necessários para operações futuras e para validar notificações. Consulte Recursos para mais detalhes sobre status da order e transação.

# Configurar notificações

As notificações **Webhooks**, também conhecidas como **devoluções de chamada web**, são um método eficaz que permitem aos servidores do Mercado Pago enviar informações em **tempo real** quando ocorre um evento específico relacionado à sua integração. Em vez de seu sistema realizar consultas constantes para verificar atualizações, os Webhooks permitem a transmissão de dados de maneira **passiva e automática** entre Mercado Pago e sua integração através de uma solicitação **HTTPS POST**, otimizando a comunicação e reduzindo a carga nos servidores.

> RED_MESSAGE
>
> No caso de integrações :toolTipComponent[para terceiros]{link="/developers/pt/docs/security/oauth/creation#bookmark_authorization_code" linkText="Authorization code" content="Integrações de Código QR ao seu sistema em nome de um vendedor e configuradas a partir de credenciais obtidas por meio do protocolo de segurança OAuth. Para mais informações, acesse o link abaixo."}, as notificações Webhooks devem ser configuradas na :toolTipComponent[aplicação]{link="/developers/pt/docs/application-details" linkText="Detalhes da aplicação" content="Entidade registrada no Mercado Pago que atua como um identificador para gerenciar suas integrações. Para mais informações, acesse o link abaixo."} da conta principal, que obteve permissões para transações em nome de terceiros.

## Configurar Webhooks

A seguir, apresentaremos um passo a passo para poder receber notificações de transações em integrações com QR Code. Uma vez configuradas, as notificações Webhook serão enviadas sempre que ocorrer um evento relevante, como transação aprovada (processamento), reembolso, falha, cancelamento ou expiração.

1. Acesse [Suas integrações](/developers/panel/app) e selecione a aplicação integrada com QR Code para a qual deseja ativar as notificações.

![cofigure notifications](/images/api-orders/not1-app-pt-v1.png)

2. No menu à esquerda, selecione **Webhooks > Configurar notificações**.

![cofigure notifications](/images/api-orders/not2-configure-pt-v1.png)

3. Selecione a aba **Modo de produção** e forneça uma `URL HTTPS` para receber notificações com sua integração produtiva.

![cofigure notifications](/images/api-orders/not3-url-pt-v1.png)

4. Selecione o evento **Order (Mercado Pago)** para receber notificações, que serão enviadas em formato `JSON` através de um `HTTPS POST` para a URL especificada anteriormente.

![cofigure notifications](/images/api-orders/not4-order-pt-v1.png)

5. Por fim, clique em **Salvar configuração**. Uma chave secreta exclusiva será gerada para a aplicação, o que permitirá validar a autenticidade das notificações recebidas garantir que sejam enviadas pelo Mercado Pago. Tenha em mente que esta chave não tem prazo de validade e sua renovação periódica não é obrigatória, embora seja recomendada. Para isso, basta clicar no botão **Redefinir**.

Ao concluir, suas notificações Webhooks para QR Code estarão configuradas e você poderá receber os seguintes alertas sobre a order:

- **Processada** (`order.processed`)
- **Cancelada** (`order.canceled`)
- **Reembolsada** (`order.refunded`)
- **Expirada** (`order.expired`)

## Simular a recepção da notificação

Para garantir que as notificações sejam configuradas corretamente, é necessário simular sua recepção. Para isso, siga o passo a passo abaixo.

1. Após configurar a URL e o evento, clique em **Salvar configuração**.
2. Depois, clique em **Simular** para testar se a URL indicada está recebendo as notificações corretamente.
3. Na tela de simulação, selecione a URL que será testada.
4. Em seguida, selecione o **tipo de evento** e insira a **identificação** que será enviada no corpo da notificação (Data ID).

![cofigure notifications](/images/api-orders/not5-order-pt-v1.png)

5. Por fim, clique em **Enviar teste** para verificar a solicitação, a resposta fornecida pelo servidor e a descrição do evento. Você receberá uma resposta conforme os exemplos abaixo, que representam o _body_ da notificação recebida em seu servidor.

::::TabsComponent

:::TabComponent{title="Aprovada"}
```json
{
  "action": "order.processed",
  "api_version": "v1",
  "application_id": "8950412930771472",
  "data": {
  "external_reference": "ExtRef_123456",
  "id": "ORD01JYHTJA9M4NKTA06K7M808NJD",
  "status": "processed",
  "status_detail": "accredited",
  "total_amount": "100.00",
  "transactions": {
  "cash_outs": [
  {
  "amount": "100.00",
  "id": "CAS01JYHTJA9M4NKTA06K7N6SM4AT",
  "reference": {
  "id": "116232980550"
  },
  "status": "processed",
  "status_detail": "accredited"
  }
  ]
  },
  "type": "qr",
  "version": 2
  },
  "date_created": "2025-06-24T20:38:14.268358898Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1898180608"
}
```
:::
:::TabComponent{title="Expirada"}
```json
{
  "action": "order.expired",
  "api_version": "v1",
  "application_id": "7364289770550796",
  "data": {
  "external_reference": "ER_123456",
  "id": "ORD01JV391F8YM8EDEAG8CWZ0GM0N",
  "status": "expired",
  "status_detail": "expired",
  "total_amount": "30.00",
  "type": "qr",
  "version": 2
  },
  "date_created": "2025-05-12T22:29:56.694526977Z",
  "live_mode": false,
  "type": "order",
  "user_id": "1403498245"
}
```
:::
:::TabComponent{title="Cancelada"}
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
:::TabComponent{title="Reembolsada"}
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
::::

## Validar a origem da notificação

A validação da origem de uma notificação é fundamental para garantir a segurança e a autenticidade das informações recebidas. Este processo ajuda a prevenir fraudes e garante que somente as notificações legítimas sejam processadas.

O Mercado Pago enviará ao seu servidor uma notificação similar ao exemplo abaixo para um alerta do tema `order`. Neste exemplo, está incluída a notificação completa, que contém os _query params_, o `body` e o `header` da notificação.

- **_Query params_**: São parâmetros de consulta que acompanham a URL. No exemplo, temos `data.id=ORD01JQ4S4KY8HWQ6NA5PXB65B3D3` e `type=order`.
- **_Body_**: O corpo da notificação contém informações detalhadas sobre o evento, como `action`, `api_version`, `application_id`, `date_created`, `id`, `live_mode`, `type`, `user_id` e `data`.
- **_Header_**: O cabeçalho contém metadados importantes, incluindo a assinatura secreta da notificação `x-signature`.

```
POST /test?data.id=ORD01JQ4S4KY8HWQ6NA5PXB65B3D3&type=order HTTP/1.1
Host: prueba.requestcatcher.com
Accept: */*
Accept-Encoding: *
Connection: keep-alive
Content-Length: 177
Content-Type: application/json
Newrelic: eyJ2IjpbMCwxXSwiZCI6eyJ0eSI6IkFwcCIsImFjIjoiOTg5NTg2IiwiYXAiOiI5NjA2MzYwOTQiLCJ0eCI6ImY4MzljZjg4ODg2MGRmZTIiLCJ0ciI6ImMwOGMwZGMyMjNjZDY2YjJkZWQwMjUxZmYxNWNiNGQ1IiwicHIiOjEuMjUwMzIsInNhIjp0cnVlLCJ0aSI6MTc0Mjg0MjU4MDE2NCwiaWQiOiIxOGI2NDcxNjNkNzI3NjU4IiwidGsiOiIxNzA5NzA3In19=
Traceparent: 00-c08c0dc223cd66b2ded0251ff15cb4d5-18b647163d727658-01
Tracestate: 1709707@nr=0-0-989586-960636094-18b647163d727658-f839cf888860dfe2-1-1.250320-1742842580164
User-Agent: restclient-node/4.15.3
X-Request-Id: 2066ca19-c6f1-498a-be75-1923005edd06
X-Rest-Pool-Name: /services/webhooks.js
X-Retry: 0
X-Signature: ts=1742505638683,v1=ced36ab6d33566bb1e16c125819b8d840d6b8ef136b0b9127c76064466f5229b
X-Socket-Timeout: 22000
{"action":"order.action_required","api_version":"v1","application_id":"76506430185983","date_created":"2021-11-01T02:02:02Z","id":"123456","live_mode":false,"type":"order","user_id":2025701502,"data":{"id":"ORD01JQ4S4KY8HWQ6NA5PXB65B3D3"}}
```

> RED_MESSAGE
>
> Embora o parâmetro `data.id` seja retornado na notificação com caracteres alfanuméricos em letra maiúscula, para utilizá-lo no processo de validação da notificação será necessário enviá-lo em letra minúscula. Ou seja, considerando o exemplo anterior, o valor `ORD01JQ4S4KY8HWQ6NA5PXB65B3D3` deverá ser utilizado como `ord01jq4s4ky8hwq6na5pxb65b3d3`.

A partir da notificação Webhook recebida, você poderá validar a autenticidade de sua origem. O Mercado Pago sempre incluirá a chave secreta nas notificações Webhooks que serão recebidas, o que permitirá validar sua autenticidade. Esta chave será enviada no _header_ `x-signature`, que será similar ao exemplo abaixo.

```
ts=1742505638683,v1=ced36ab6d33566bb1e16c125819b8d840d6b8ef136b0b9127c76064466f5229b
```

Para confirmar a validação, é necessário extrair a chave contida no cabeçalho e compará-la com a chave fornecida para sua aplicação em [Suas integrações](/developers/panel/app). Para isso, siga o passo a passo abaixo. Ao final, disponibilizamos nossos SDKs com exemplos de códigos completos para facilitar o processo.

1. Para extrair o timestamp (`ts`) e a chave (`v1`) do _header_ `x-signature`, divida o conteúdo do _header_ pelo caractere “,", o que resultará em uma lista de elementos. O valor para o prefixo `ts` é o _timestamp_ (em milissegundos) da notificação e `v1` é a chave encriptada. Seguindo o exemplo apresentado anteriormente, `ts=1742505638683` e `v1=ced36ab6d33566bb1e16c125819b8d840d6b8ef136b0b9127c76064466f5229b`.
2. Utilizando o _template_ abaixo, substitua os parâmetros com os dados recebidos na sua notificação.

```
id:[data.id_url];request-id:[x-request-id_header];ts:[ts_header];
```

- Os parâmetros com o sufixo `_url` provêm de _query params_. Exemplo: [`data.id_url`] será substituído pelo valor correspondente ao ID do evento (`data.id`). **Este _query param_ pode ser encontrado na notificação recebida em letra maiúscula, mas deverá ser utilizado em minúscula.** No exemplo de notificação mencionado anteriormente, o `data.id_url` é `ORD01JQ4S4KY8HWQ6NA5PXB65B3D3` e deverá ser utilizado como `ord01jq4s4ky8hwq6na5pxb65b3d3`.
- [`x-request-id_header`] deverá ser substituído pelo valor recebido no _header_ `x-request-id`. No exemplo de notificação mencionado anteriormente, o `x-request-id` é `2066ca19-c6f1-498a-be75-1923005edd06`.
- [`ts_header`] será o valor `ts` extraído do _header_ `x-signature`. No exemplo de notificação mencionado anteriormente, o `ts` é `1742505638683`.
- Ao aplicar os dados ao template, ficaria da seguinte forma:
`id:ord01jq4s4ky8hwq6na5pxb65b3d3;request-id:2066ca19-c6f1-498a-be75-1923005edd06;ts:1742505638683;`

> WARNING
>
> Se algum dos valores apresentados no modelo anterior não estiver presente na notificação recebida, você deve removê-lo.
3. Em [Suas integrações](/developers/panel/app), selecione a aplicação integrada, clique em **Webhooks > Configurar notificação** e revele a chave secreta gerada.

![cofigure notifications](/images/api-orders/not6-signature-pt-v1.png)

4. Gere a contrachave para validação. Para fazer isso, calcule um [HMAC](https://pt.wikipedia.org/wiki/HMAC) com a função de `hash SHA256` em base hexadecimal, utilizando a assinatura secreta como chave e o template com os valores como mensagem.

[[[
```php
$cyphedSignature = hash_hmac('sha256', $data, $key);
```
```node
const crypto = require('crypto');
const cyphedSignature = crypto
  .createHmac('sha256', secret)
  .update(signatureTemplateParsed)
  .digest('hex'); 
```
```java
String cyphedSignature = new HmacUtils("HmacSHA256", secret).hmacHex(signedTemplate);
```
```python
import hashlib, hmac, binascii

cyphedSignature = binascii.hexlify(hmac_sha256(secret.encode(), signedTemplate.encode()))
```
]]]

5. Finalmente, compare a chave gerada com a chave extraída do _header_, assegurando-se de que tenham uma correspondência exata. Além disso, você pode usar o _timestamp_ extraído do _header_ para compará-lo com um _timestamp_ gerado no momento da recepção da notificação. Isso permite estabelecer uma margem de tolerância para atrasos no recebimento da mensagem.

Veja exemplos de códigos completos abaixo:

[[[
```php
<?php
// Obtain the x-signature value from the header
$xSignature = $_SERVER['HTTP_X_SIGNATURE'];
$xRequestId = $_SERVER['HTTP_X_REQUEST_ID'];

// Obtain Query params related to the request URL
$queryParams = $_GET;

// Extract the "data.id" from the query params
$dataID = isset($queryParams['data.id']) ? $queryParams['data.id'] : '';

// Separating the x-signature into parts
$parts = explode(',', $xSignature);

// Initializing variables to store ts and hash
$ts = null;
$hash = null;

// Iterate over the values to obtain ts and v1
foreach ($parts as $part) {
  // Split each part into key and value
  $keyValue = explode('=', $part, 2);
  if (count($keyValue) == 2) {
  $key = trim($keyValue[0]);
  $value = trim($keyValue[1]);
  if ($key === "ts") {
  $ts = $value;
  } elseif ($key === "v1") {
  $hash = $value;
  }
  }
}

// Obtain the secret key for the user/application from Mercadopago developers site
$secret = "your_secret_key_here";

// Generate the manifest string
$manifest = "id:$dataID;request-id:$xRequestId;ts:$ts;";

// Create an HMAC signature defining the hash type and the key as a byte array
$sha = hash_hmac('sha256', $manifest, $secret);
if ($sha === $hash) {
  // HMAC verification passed
  echo "HMAC verification passed";
} else {
  // HMAC verification failed
  echo "HMAC verification failed";
}
?>
```
```javascript
// Obtain the x-signature value from the header
const xSignature = headers['x-signature']; // Assuming headers is an object containing request headers
const xRequestId = headers['x-request-id']; // Assuming headers is an object containing request headers

// Obtain Query params related to the request URL
const urlParams = new URLSearchParams(window.location.search);
const dataID = urlParams.get('data.id');

// Separating the x-signature into parts
const parts = xSignature.split(',');

// Initializing variables to store ts and hash
let ts;
let hash;

// Iterate over the values to obtain ts and v1
parts.forEach(part => {
  // Split each part into key and value
  const [key, value] = part.split('=');
  if (key && value) {
  const trimmedKey = key.trim();
  const trimmedValue = value.trim();
  if (trimmedKey === 'ts') {
  ts = trimmedValue;
  } else if (trimmedKey === 'v1') {
  hash = trimmedValue;
  }
  }
});

// Obtain the secret key for the user/application from Mercadopago developers site
const secret = 'your_secret_key_here';

// Generate the manifest string
const manifest = `id:${dataID};request-id:${xRequestId};ts:${ts};`;

// Create an HMAC signature
const hmac = crypto.createHmac('sha256', secret);
hmac.update(manifest);

// Obtain the hash result as a hexadecimal string
const sha = hmac.digest('hex');

if (sha === hash) {
  // HMAC verification passed
  console.log("HMAC verification passed");
} else {
  // HMAC verification failed
  console.log("HMAC verification failed");
}
```
```python
import hashlib
import hmac
import urllib.parse

# Obtain the x-signature value from the header
xSignature = request.headers.get("x-signature")
xRequestId = request.headers.get("x-request-id")

# Obtain Query params related to the request URL
queryParams = urllib.parse.parse_qs(request.url.query)

# Extract the "data.id" from the query params
dataID = queryParams.get("data.id", [""])[0]

# Separating the x-signature into parts
parts = xSignature.split(",")

# Initializing variables to store ts and hash
ts = None
hash = None

# Iterate over the values to obtain ts and v1
for part in parts:
  # Split each part into key and value
  keyValue = part.split("=", 1)
  if len(keyValue) == 2:
  key = keyValue[0].strip()
  value = keyValue[1].strip()
  if key == "ts":
  ts = value
  elif key == "v1":
  hash = value

# Obtain the secret key for the user/application from Mercadopago developers site
secret = "your_secret_key_here"

# Generate the manifest string
manifest = f"id:{dataID};request-id:{xRequestId};ts:{ts};"

# Create an HMAC signature defining the hash type and the key as a byte array
hmac_obj = hmac.new(secret.encode(), msg=manifest.encode(), digestmod=hashlib.sha256)

# Obtain the hash result as a hexadecimal string
sha = hmac_obj.hexdigest()
if sha == hash:
  # HMAC verification passed
  print("HMAC verification passed")
else:
  # HMAC verification failed
  print("HMAC verification failed")
```
```go
import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"net/http"
	"strings"
)

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		// Obtain the x-signature value from the header
		xSignature := r.Header.Get("x-signature")
		xRequestId := r.Header.Get("x-request-id")

		// Obtain Query params related to the request URL
		queryParams := r.URL.Query()

		// Extract the "data.id" from the query params
		dataID := queryParams.Get("data.id")

		// Separating the x-signature into parts
		parts := strings.Split(xSignature, ",")

		// Initializing variables to store ts and hash
		var ts, hash string

		// Iterate over the values to obtain ts and v1
		for _, part := range parts {
			// Split each part into key and value
			keyValue := strings.SplitN(part, "=", 2)
			if len(keyValue) == 2 {
				key := strings.TrimSpace(keyValue[0])
				value := strings.TrimSpace(keyValue[1])
				if key == "ts" {
					ts = value
				} else if key == "v1" {
					hash = value
				}
			}
		}

		// Get secret key/token for specific user/application from Mercadopago developers site
		secret := "your_secret_key_here"

		// Generate the manifest string
		manifest := fmt.Sprintf("id:%v;request-id:%v;ts:%v;", dataID, xRequestId, ts)

		// Create an HMAC signature defining the hash type and the key as a byte array
		hmac := hmac.New(sha256.New, []byte(secret))
		hmac.Write([]byte(manifest))

		// Obtain the hash result as a hexadecimal string
		sha := hex.EncodeToString(hmac.Sum(nil))

if sha == hash {
  // HMAC verification passed
  fmt.Println("HMAC verification passed")
} else {
  // HMAC verification failed
  fmt.Println("HMAC verification failed")
}

	})
}
```
]]]

## Ações necessárias após receber a notificação

Quando você recebe uma notificação em sua plataforma, o Mercado Pago espera uma resposta para validar que essa recepção foi correta. Para isso, você deve devolver um `HTTP STATUS 200 (OK)` ou `201 (CREATED)`.

O **tempo de espera** para essa confirmação será de **22 segundos**. Se essa confirmação não for enviada, o sistema entenderá que a notificação não foi recebida e realizará uma nova tentativa de envio **a cada 15 minutos**, até que receba a resposta. Após a terceira tentativa, o prazo será prorrogado, mas os envios continuarão acontecendo.

Após responder à notificação Webhook e confirmar seu recebimento, somente se as informações recebidas não forem suficientes e você necessite de informações adicionais, é possível obter todos os dados sobre o recurso notificado enviando um **GET** ao endpoint :TagComponent{tag="API" text="/v1/orders/{id}" href="/developers/pt/reference/in-person-payments/qr-code/orders/get-order/get"}.

Com essa informação, você poderá realizar as atualizações necessárias em sua plataforma, como atualizar uma transação aprovada.