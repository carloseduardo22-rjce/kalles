# Configurar notificações

As notificações **Webhooks**, também conhecidas como **devoluções de chamada web**, são um método eficaz que permitem aos servidores do Mercado Pago enviar informações em **tempo real** quando ocorre um evento específico relacionado à sua integração. Em vez de seu sistema realizar consultas constantes para verificar atualizações, os Webhooks permitem a transmissão de dados de maneira **passiva e automática** entre Mercado Pago e sua integração através de uma solicitação **HTTPS POST**, otimizando a comunicação e reduzindo a carga nos servidores.

## Configurar Webhooks

A seguir, apresentaremos um passo a passo para poder receber notificações de pagamento em integrações com Código QR. Uma vez configuradas, as notificações Webhook serão enviadas sempre que ocorrer um evento relevante, como pagamento aprovado (processamento), reembolso, falha, cancelamento ou expiração.

> RED_MESSAGE
>
> Se você estiver desenvolvendo utilizando :toolTipComponent[credenciais de teste]{link="/developers/es/docs/qr-code/resources/credentials" linkText="Credenciais" content="Chaves de acesso únicas com as quais identificamos uma integração na sua conta, vinculadas à sua aplicação. Para mais informações, acesse o link abaixo."}, acesse o [Mercado Pago Developers](/developers/pt/docs), faça login com o usuário e a senha do **Seller Test User** (disponíveis em **Informações gerais** da sua aplicação) e configure os Webhooks em modo produção para essa conta de teste. Dessa forma, você conseguirá testar as notificações corretamente. Por outro lado, no caso de integrações :toolTipComponent[para terceiros]{link="/developers/pt/docs/security/oauth/creation#bookmark_authorization_code" linkText="Authorization code" content="Integrações de Código QR ao seu sistema em nome de um vendedor e configuradas a partir de credenciais obtidas por meio do protocolo de segurança OAuth. Para mais informações, acesse o link abaixo."}, as notificações Webhooks devem ser configuradas na :toolTipComponent[aplicação]{link="/developers/pt/docs/application-details" linkText="Detalhes da aplicação" content="Entidade registrada no Mercado Pago que atua como um identificador para gerenciar suas integrações. Para mais informações, acesse o link abaixo."} da sua conta principal como integrador, que obteve permissões para transações em nome de terceiros.

1. Acesse [Suas integrações](/developers/panel/app) e selecione a aplicação integrada com Código QR para a qual deseja ativar as notificações.

![cofigure notifications](/images/api-orders/not1-app-pt-v1.png)

2. No menu à esquerda, selecione **Webhooks > Configurar notificações**.

![cofigure notifications](/images/api-orders/not2-configure-pt-v1.png)

3. Selecione a aba **Modo de produção** e forneça uma `URL HTTPS` para receber notificações com sua integração produtiva.

![cofigure notifications](/images/api-orders/not3-url-pt-v1.png)

4. Selecione o evento **Order (Mercado Pago)** para receber notificações, que serão enviadas em formato `JSON` através de um `HTTPS POST` para a URL especificada anteriormente.

![cofigure notifications](/images/api-orders/not4-order-pt-v1.png)

5. Por fim, clique em **Salvar configuração**. Uma chave secreta exclusiva será gerada para a aplicação, o que permitirá validar a autenticidade das notificações recebidas e garantir que sejam enviadas pelo Mercado Pago. Tenha em mente que esta chave não tem prazo de validade e sua renovação periódica não é obrigatória, embora seja recomendada. Para isso, basta clicar no botão **Redefinir**.

Ao concluir, suas notificações Webhooks para Código QR estarão configuradas e você poderá receber os seguintes alertas sobre a order:

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

:::TabComponent{title="Procesada"}

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
> Se algum dos valores apresentados no modelo anterior não estiver presente na notificação recebida, você deve removê-lo. 3. Em [Suas integrações](/developers/panel/app), selecione a aplicação integrada, clique em **Webhooks > Configurar notificação** e revele a chave secreta gerada.

![cofigure notifications](/images/api-orders/not6-signature-pt-v1.png)

4. Gere a contrachave para validação. Para fazer isso, calcule um [HMAC](https://pt.wikipedia.org/wiki/HMAC) com a função de `hash SHA256` em base hexadecimal, utilizando a assinatura secreta como chave e o template com os valores como mensagem.

[[[

```php
$cyphedSignature = hash_hmac('sha256', $data, $key);
```

```node
const crypto = require("crypto");
const cyphedSignature = crypto
  .createHmac("sha256", secret)
  .update(signatureTemplateParsed)
  .digest("hex");
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
const xSignature = headers["x-signature"]; // Assuming headers is an object containing request headers
const xRequestId = headers["x-request-id"]; // Assuming headers is an object containing request headers

// Obtain Query params related to the request URL
const urlParams = new URLSearchParams(window.location.search);
const dataID = urlParams.get("data.id");

// Separating the x-signature into parts
const parts = xSignature.split(",");

// Initializing variables to store ts and hash
let ts;
let hash;

// Iterate over the values to obtain ts and v1
parts.forEach((part) => {
  // Split each part into key and value
  const [key, value] = part.split("=");
  if (key && value) {
    const trimmedKey = key.trim();
    const trimmedValue = value.trim();
    if (trimmedKey === "ts") {
      ts = trimmedValue;
    } else if (trimmedKey === "v1") {
      hash = trimmedValue;
    }
  }
});

// Obtain the secret key for the user/application from Mercadopago developers site
const secret = "your_secret_key_here";

// Generate the manifest string
const manifest = `id:${dataID};request-id:${xRequestId};ts:${ts};`;

// Create an HMAC signature
const hmac = crypto.createHmac("sha256", secret);
hmac.update(manifest);

// Obtain the hash result as a hexadecimal string
const sha = hmac.digest("hex");

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

Com essa informação, você poderá realizar as atualizações necessárias em sua plataforma, como atualizar um pagamento aprovado.
