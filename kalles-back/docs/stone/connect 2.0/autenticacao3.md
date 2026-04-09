Autenticação

# Autenticação

</br>Antes de começar, você precisa obter suas chaves de API.\
Para isso, siga os seguintes passos:\
**1** - Acesse este [**link**](https://id.pagar.me) e faça login com seu usuário,\
**2** - Após acessar o Dash, navegue até a área de **Desenvolvimento** e em seguida clique em **Chaves**.

## Tipos de Chave

> 🚧 Ambiente de Sandbox
>
> O ambiente de testes é aplicável somente no ambiente de criação de pedidos na API do Pagar.me.
>
> Dado que não possuímos ambiente de testes na Stone, os pedidos criados no Pagar.me em ambiente de Sandbox não serão refletidos no POS.
>
> Com isso, **não possuímos um ambiente de Sandbox para realizar testes de ponta a ponta no Connect Stone.** e os pedidos devem ser criados diretamente no ambiente de produção.

Com a conclusão do processo de credenciamento, serão enviadas as chaves, porém, você também pode visualizá-la via dashboard Pagar.me, conforme orientado [neste link](https://stone-partnerprogram.helpjuice.com/pt_BR/connect-stone1/criar-sk-para-integra%C3%A7%C3%A3o):

- Exemplo de prefixo da **Chave Secreta** de Produção: `sk_*`
- Exemplo de prefixo da **Chave Pública** de Produção: `pk_*`

[block:html]
{
"html": "</br>"
}
[/block]

## Estrutura de Credenciais

O Connect Stone se comunica com duas estruturas: da **Pagar.me** e da **Stone**.\
No Pagar.me, vocês como parceiros possuem uma [merchant](https://connect-stone.stone.com.br/docs/conceitos), e cada um dos seus clientes (estabelecimentos) são uma [account](https://connect-stone.stone.com.br/docs/conceitos), que por sua vez possui uma chave transacional, [Secret_Key](https://connect-stone.stone.com.br/docs/conceitos) vinculada. Esta **account** está associada à um **Stonecode**, que é a identificação do estabelecimento na Stone. Dentro deste registro são vinculados os POS, as máquinas que serão usadas para transacionar.

No diagrama abaixo você consegue entender como cada participante está vinculado e suas nomenclaturas:

![](https://files.readme.io/022013b-estrutura.JPG)

[block:html]
{
"html": "</br>"
}
[/block]

## Autorização Basic Auth

Para se autenticar conosco você deve enviar a **Chave de API** no cabeçalho **Authorization**, seguindo o padrão da [HTTP Basic Authentication](https://en.wikipedia.org/wiki/Basic_access_authentication).

```curl
curl --location --request POST  https://api.pagar.me/core/v5/orders' \
     -H 'Authorization: Basic c2tfdGVzdF9*Og==' \
     -H 'Content-Type: application/json' \
```

```javascript Node.js
var fs = require("fs");
const request = require("request");
var body = JSON.parse(fs.readFileSync("body.json", "utf8"));

var options = {
  method: "POST",
  uri: "https://api.pagar.me/core/v5/orders",
  headers: {
    Authorization: "Basic " + Buffer.from("sk_test_*:").toString("base64"),
    "Content-Type": "application/json",
  },
  json: body,
};

request(options, function (error, response, body) {
  console.log(response.body);
});
```

> 📘 Como utilizar a SecretKey
>
> Para montar a requisição Basic Auth, você deve utilizar a SecretKey da seguinte maneira:\
> **User:** SecretKey\
> **Password:** _vazio_

> ❗️ NÃO COMPARTILHE SUAS CHAVES DE API
>
> A sua chave da API é **SECRETA** e não deve ser compartilhada com terceiros.
