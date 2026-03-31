# Configurar terminal

Para continuar com a integração do Mercado Pago Point ao seu sistema, após [criar a sua aplicação](/developers/pt/docs/mp-point/create-application) e obter as credenciais adequadas, é necessário que você configure seu terminal Point para operar em modo integrado.

É essa configuração que garante que os pagamentos realizados nos terminals possam ser gerenciados a partir de seu sistema, otimizando a eficiência na conciliação e na gestão de tarefas.

Para realizar a configuração do seu terminal Point em modo integrado, primeiro você deverá **criar e configurar uma loja e um caixa**, e depois **associar esse terminal à loja e caixa criados**. Isso permite que cada leitor esteja vinculado não apenas a uma conta do Mercado Pago, mas também a um ponto de venda físico identificado em nosso sistema.

Por último, com seu terminal já vinculado, você deverá **ativar seu modo de operação como Ponto de Venda (PDV)**. Siga as instruções abaixo para realizar corretamente cada passo.

:::::AccordionComponent{title="Criar e configurar loja e caixa" pill="1"}

A criação de lojas e caixas no Mercado Pago é necessária para poder operar em lojas físicas com terminals Point e assim poder manter a conciliação entre seu ponto de venda e o Mercado Pago.

Uma **loja** representa uma loja física dentro do Mercado Pago, que pode ter um ou mais caixas vinculados. No entanto, **cada caixa permite apenas um terminal associado em modo PDV**. Isso significa que, se você está querendo integrar mais de um terminal, deverá criar a mesma quantidade de caixas e realizar sua associação de maneira individual.

A criação e a configuração de lojas e caixas podem ser realizadas por duas vias: a partir do painel do Mercado Pago ou via API. Esta última opção é útil para sistemas que requeiram operar com vários pontos de venda, já que permite associar várias lojas a partir do sistema integrador.

Escolha a via que melhor se adeque às suas necessidades e siga os passos detalhados conforme o caso.

::::TabsComponent

:::TabComponent{title="API"}
É possível criar lojas e caixas a partir do seu sistema através de nossas APIs para pagamentos presenciais. Para isso, siga os passos abaixo.

### Criar loja

Para criar uma loja via API, envie um **POST** com o :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark*acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."} ao endpoint :TagComponent{tag="API" text="Criar loja" href="/developers/pt/reference/in-person-payments/point/stores/create-store/post"}. Você deverá adicionar o :toolTipComponent[`user_id` da conta de teste]{content="Durante o desenvolvimento da integração, utilize o User ID referente à conta de teste, disponível em _Suas integrações > Detalhes da aplicação > Credenciais de teste > Dados das credenciais de teste_. Ao subir em produção, substitua-o pelo User ID da conta real do Mercado Pago que receberá os pagamentos."} no path da sua solicitação e completar os parâmetros requeridos com os detalhes do negócio conforme se indica a seguir.

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
  "street_name": "Nome da Rua de Exemplo.",
  "city_name": "Nome da cidade.",
  "state_name": "Nome do estado.",
  "latitude": 27.175193925922862,
  "longitude": 78.04213533235064,
  "reference": "Próximo ao Mercado Pago"
  }
}'
```

| Parâmetro     | Obrigatoriedade | Descrição e exemplos                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| ------------- | --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `user_id`     | Obrigatório     | Identificador da conta do Mercado Pago que recebe o dinheiro pelas vendas realizadas na loja.<br><br>Durante o desenvolvimento, utilize o `user_id` da conta de teste, disponível em **Suas integrações > Detalhes da aplicação > Credenciais de teste > Dados das credenciais de teste**.<br><br>Ao subir em produção, substitua pelo `user_id` da conta real que receberá os pagamentos: Se você está realizando uma integração :toolTipComponent[própria]{content="Integrações de Mercado Pago Point ao seu sistema para uso próprio e configuradas a partir das credenciais da sua aplicação."}, encontrará este valor nos [Detalhes da aplicação](/developers/pt/docs/mp-point/resources/application-details). Se, ao contrário, está realizando uma integração :toolTipComponent[para terceiros]{content="Integrações de Mercado Pago Point ao seu sistema em nome de um vendedor e configuradas a partir de credenciais obtidas por meio do protocolo de segurança OAuth."}, obterá o valor na resposta à :toolTipComponent[vinculação por meio de OAuth]{link="/developers/pt/docs/mp-point/resources/security/landing-hub" linkText="OAuth" content="Chave privada gerada mediante o protocolo de segurança OAuth, que permite gerenciar integrações em nome de terceiros. Para mais informações, dirija-se à documentação."}. |
| `name`        | Obrigatório     | Nome da loja criada.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `external_id` | Opcional        | Identificador externo da loja para o sistema do integrador. Pode conter qualquer valor alfanumérico de até 60 caracteres, e deve ser único para cada loja. Por exemplo, `LOJMercadoPago`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `location`    | Obrigatório     | Este objeto deve conter todas as informações da localização da loja. É importante preencher tudo corretamente, especialmente `latitude` e `longitude`, usando o formato decimal simples e os dados reais do local. Por exemplo, `"latitude": 27.175193925922862`, e `"longitude": 78.04213533235064` correspondem à localização exata do Taj Mahal, na Índia.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |

Se a solicitação foi enviada corretamente, a resposta será como o exemplo a seguir.

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
    "address_line": "Nome da Rua de Exemplo, 0123, Nome da cidade, Nome do estado.",
    "latitude": 27.175193925922862,
    "longitude": 78.04213533235064,
    "reference": "Próximo ao Mercado Pago"
  },
  "external_id": "LOJ001"
}
```

Além dos dados enviados na solicitação, retornará o identificador atribuído a essa loja pelo Mercado Pago sob o parâmetro `id`.

### Criar caixa

Para poder realizar vendas com o Mercado Pago, cada loja criada deverá conter pelo menos um caixa associado. É possível criar um caixa e associá-lo à loja previamente criada enviando um **POST** com o :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark*acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."} ao endpoint :TagComponent{tag="API" text="Criar caixa" href="/developers/pt/reference/in-person-payments/point/pos/create-pos/post"} como mostrado a seguir.

```curl
curl -X POST \
  'https://api.mercadopago.com/pos'\
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -d '{
  "name": "Primeiro POS",
  "store_id": "12354567",
  "external_store_id": "LOJ001",
  "external_id": "LOJ001POS001",
  "category": 621102
}'
```

| Parâmetro           | Obrigatoriedade | Descrição e exemplos                                                                                                                                                                                                |
| ------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `name`              | Obrigatório     | Nome do caixa criado.                                                                                                                                                                                               |
| `store_id`          | Obrigatório     | Identificador da loja à qual pertence o caixa, atribuído a essa loja pelo Mercado Pago. É retornado na resposta à criação da loja sob o parâmetro `id`.                                                             |
| `external_store_id` | Opcional        | Identificador externo da loja, que foi atribuído pelo sistema do integrador no momento de sua criação sob o parâmetro `external_id`.                                                                                |
| `external_id`       | Obrigatório     | Identificador externo do caixa, definido para o sistema integrador. Deve ser um valor único para cada caixa e tem um limite de 40 caracteres.                                                                       |
| `category`          | Obrigatório     | Código MCC que indica o ramo ao qual pertence o ponto de venda. Você pode consultar a lista completa de opções em nossa [Referência de API](/developers/pt/reference/in-person-payments/point/pos/create-pos/post). |

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
  "name": "Primeiro POS",
  "fixed_amount": false,
  "category": 621102,
  "store_id": "12354567",
  "external_store_id": "LOJ001",
  "external_id": "LOJ001POS001"
}
```

Você pode ver na tabela abaixo a descrição de alguns dos parâmetros retornados que podem ser úteis para continuar com sua integração mais adiante.

| Parâmetro           | Descrição                                                                                                                                                                      |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `id`                | Identificador atribuído ao caixa pelo Mercado Pago.                                                                                                                            |
| `qr`                | Objeto que conterá um QR code associado ao caixa criado. Só poderá ser utilizado caso tenha integrada a solução de pagamento [QR Code ](/developers/pt/docs/qr-code/overview). |
| `status`            | _Status_ no qual se encontra a criação do ponto de venda.                                                                                                                      |
| `user_id`           | Identificador da conta do Mercado Pago que recebe o dinheiro pelas vendas realizadas no caixa.                                                                                 |
| `name`              | Nome atribuído ao caixa no momento de sua criação.                                                                                                                             |
| `store_id`          | Identificador da loja à qual pertence o caixa, atribuído a essa loja pelo Mercado Pago. É retornado na resposta à criação da loja sob o parâmetro `id`.                        |
| `external_store_id` | Identificador externo da loja, que foi atribuído pelo sistema do integrador no momento de sua criação sob o parâmetro `external_id`.                                           |
| `external_id`       | Identificador externo do caixa, definido para o sistema integrador no momento de sua criação.                                                                                  |

> NOTE
>
> Lembre-se de que só é possível configurar um terminal em modo PDV por caixa. Se deseja integrar vários terminals, deverá criar um caixa para cada um deles.

Se ambas as solicitações foram bem-sucedidas, você terá criado e configurado a loja e o caixa necessários para continuar com a configuração do seu terminal em modo PDV.

:::
:::TabComponent{title="Painel do Mercado Pago"}
A criação de lojas e caixas por meio do Painel do Mercado Pago é a opção para quem deve gerenciar apenas um ponto de venda e prefere simplificar o processo. Para fazê-lo, acesse sua [conta do Mercado Pago](https://www.mercadopago[FAKER][URL][DOMAIN]/home) e siga as instruções abaixo.

1. No menu lateral esquerdo, dirija-se a **Seu negócio > Lojas e caixas**.
2. Na seção "Lista de lojas", clique no botão **+ Adicionar loja**.
3. Complete o **Nome** e o **Endereço** do seu novo local, adicione opcionalmente **Dados extras**, caso precise de maior detalhamento, e clique em **Continuar**.
4. Complete as informações requeridas com o **horário de funcionamento da loja**, selecionando os dias e horários de abertura. Ao terminar, clique em **Continuar**.

> SUCCESS_MESSAGE
>
> As opções disponíveis nesta tela se adequam a múltiplos cenários, permitindo selecionar um único horário para todos os dias da semana ou um horário diferente para cada um, junto com a possibilidade de inserir horários especiais para feriados. Escolha a opção que melhor se adapte à sua loja.

5. A seguir, você se encontrará na tela para "Criar caixas" associados ao local, onde deverá atribuir um **Nome**. Se desejar, pode criar mais de um caixa.

> NOTE
>
> Lembre-se de que só é possível configurar um terminal em modo PDV por caixa. Se deseja integrar vários terminals, deverá criar um caixa para cada um deles.

6. Clique no botão **Continuar** e pronto! Você já criou e configurou sua loja e seu caixa.

:::
::::

:::::
:::AccordionComponent{title="Associar terminal" pill="2"}

A associação do terminal Point à conta do Mercado Pago e à loja e caixa criados deve ser realizada a partir da aplicação do Mercado Pago no **dispositivo móvel da conta recebedora ou daquela indicada como colaboradora**. Esta aplicação está disponível para dispositivos Android ou iOS.

Comece ligando o terminal Point. Você verá na tela a mensagem "**Inicie sessão neste dispositivo com sua conta do Mercado Pago**". Ali você deverá escolher entre as opções abaixo.

- **Sou responsável pelo negócio**: selecione esta opção se você é o dono da loja física.
- **Sou um colaborador**: escolha esta opção se sua conta foi indicada como conta de colaborador pelo proprietário da loja.

Uma vez selecionada a opção que corresponda, aparecerá um QR code na tela do terminal que você deverá escanear com a aplicação móvel do Mercado Pago.

Para isso, acesse a aplicação e inicie sessão com a conta de teste de vendedor, cujo usuário e senha estão disponíveis em **Suas integrações > Detalhes da aplicação > Credenciais de teste > Dados das credenciais de teste**. Em seguida, pressione o **ícone QR** na margem inferior e escaneie o código apresentado pelo terminal.

> NOTE
>
> Na etapa [Subir em produção](/developers/pt/docs/mp-point/go-to-production), você deverá repetir este processo de associação utilizando a conta recebedora ou aquela indicada como conta de colaborador, conforme o caso.

Após alguns segundos, o terminal poderá solicitar algumas configurações adicionais para a loja. Siga as instruções exibidas na tela para concluir todas as etapas.

O terminal solicitará que você selecione a loja e o caixa aos quais quer associá-lo, e confirme o endereço da loja previamente criada com sua conta do Mercado Pago. Ao finalizar, pressione o botão **Confirmar**.

> NOTE
>
> Se, por alguma razão, você tem mais de uma loja criada, atente-se de selecionar corretamente aquela que quer integrar ao seu terminal Point.

Por último, o terminal solicitará que você insira uma senha que garantirá seu uso com mais segurança.

Uma vez finalizado este processo, a tela exibirá a mensagem "Pronto! Já pode cobrar com seu Point", e você terá finalizado a associação do seu terminal à conta do Mercado Pago desejada, e à loja e caixa criados.

:::
:::AccordionComponent{title="Ativar o modo PDV no terminal" pill="3"}

Como último passo da configuração de terminals, e para que estes possam estar integrados com nossa API, é necessário ativar o modo de operação como Ponto de Venda (PDV).

Para ativar o modo PDV via API pela primeira vez, é necessário consultar os terminals disponíveis ativos em sua conta. Para isso, envie um **GET** ao endpoint :TagComponent{tag="API" text="Obter terminals" href="/developers/pt/reference/in-person-payments/point/terminals/get-terminals/get"}, utilizando o :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark*acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."}. Recomendamos o uso opcional dos parâmetros `store_id` e `pos_id` para filtrar os resultados - esses identificadores são retornados na criação da loja e do caixa, respectivamente.

```curl
curl -X GET \ 'https://api.mercadopago.com/terminals/v1/list?limit=50&offset=0&store_id=12354567&pos_id=23545678'\
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
```

Esta chamada retornará uma lista de terminals vinculados à conta do Mercado Pago, junto com seu respectivo caixa e loja associados, e seu modo de operação.

```json
{
  "data": {
    "terminals": [
      {
        "id": "NEWLAND_N950__N950NCB801293324",
        "pos_id": "23545678",
        "store_id": "12354567",
        "external_pos_id": "LOJ0101POS",
        "operating_mode": "PDV | STANDALONE | UNDEFINED"
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

| Parâmetro                  | Descrição                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| -------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `terminals.id`             | Identificador único do terminal. O formato no qual este campo é retornado é **"tipo de terminal + "\_\_" + serial do terminal"**. Por exemplo, "NEWLAND_N950\_\_N950NCB801293324". Você poderá identificar o Point que deseja por meio dos últimos caracteres deste campo, que deverão coincidir com o serial que aparece na etiqueta traseira do terminal físico.                                                                                                      |
| `terminals.pos_id`         | Identificador do caixa ao qual está associado o terminal Point.                                                                                                                                                                                                                                                                                                                                                                                                         |
| `terminals.store_id`       | Identificador da loja à qual está associado o terminal Point.                                                                                                                                                                                                                                                                                                                                                                                                           |
| `terminals.operating_mode` | Modo de operação no qual está funcionando o terminal no momento da consulta. Pode ser: <br><br> - **PDV**: modo de operação como Ponto de Venda (PDV). É o modo no qual opera o terminal quando está integrado via API e só poderá receber pagamentos com cartões. <br> - **STANDALONE**: configuração do terminal por padrão. É o modo no qual opera quando **não** está integrado via API. <br> - **UNDEFINED**: a configuração que tem o terminal não é reconhecida. |

Como o único modo de operação que permite integrar os terminals via API é o PDV, uma vez que tenha localizado o terminal Point desejado, você deverá ativá-lo. Para isso, envie um **PATCH** com o :toolTipComponent[Access Token de teste]{link="/developers/pt/docs/mp-point/create-application#bookmark*acessar_as_credenciais_de_teste" linkText="Acessar as credenciais de teste" content="Chave privada da aplicação criada no Mercado Pago, utilizada no \_backend* durante o desenvolvimento da integração. Você pode acessá-la em _Suas integrações > Detalhes da aplicação > Testes > Credenciais de teste_. Ao subir em produção, substitua-o pelo Access Token de produção se se tratar de uma integração própria, ou pelo Access Token obtido mediante OAuth no caso de integrações de terceiros."} ao endpoint :TagComponent{tag="API" text="Alterar o modo de operação" href="/developers/pt/reference/orders/in-person-payments/point/terminals/update-operation-mode/patch"}, como mostrado a seguir.

> NOTE
>
> Caso o terminal consultado já esteja com o modo PDV ativado, a solicitação deve ser ignorada e o [processamento de pagamentos](/developers/pt/docs/mp-point/payment-processing) pode seguir normalmente; em caso de reconfiguração, o modo PDV deve ser ajustado diretamente no leitor, acessando **Mais opções > Configurações > Modo de vinculação**.

```curl
curl -X PATCH \
  'https://api.mercadopago.com/terminals/v1/setup'\
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -d '{
  "terminals": [
  {
  "id": "NEWLAND_N950__N950NCB801293324",
  "operating_mode": "PDV"
  }
  ]
}'
```

| Campo                      | Tipo     | Descrição                                                                                                                                                                                                                                                                                         |
| -------------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `terminals.id`             | _String_ | Identificador único do terminal cujo modo de operação se quer modificar, obtido na solicitação para consultar os terminals disponíveis. Você deve enviá-lo seguindo o formato **"tipo de terminal + "\_\_" + serial do terminal"**, como no seguinte exemplo: "NEWLAND_N950\_\_N950NCB801293324". |
| `terminals.operating_mode` | _String_ | Modo operativo no qual você quer configurar o terminal. Para integrar seu terminal via API, o valor deve ser `PDV`, que corresponde ao modo de operação com Ponto de Venda.                                                                                                                       |

Se a solicitação foi bem-sucedida, a resposta deverá retornar o parâmetro `operating_mode=PDV`.

```json
{
  "terminals": [
  {
  "id": "NEWLAND_N950__N950NCB801293324",
  "operating_mode": PDV"
  }
  ]
}
```

> NOTE
>
> Lembre-se de que só é possível configurar um terminal em modo PDV por caixa. Se deseja integrar vários terminals, deverá criar um caixa para cada um deles.

Para finalizar a configuração do seu terminal, você deverá reiniciá-lo e, em seguida, verificar se foi exitosa dirigindo-se a **Mais opções > Configurações > Modo de vinculação**. Se encontrar que o modo de vinculação é **Ponto de Venda (PDV)**, a mudança no modo de operação foi efetiva e você poderá continuar integrando o processamento de pagamentos.
:::
