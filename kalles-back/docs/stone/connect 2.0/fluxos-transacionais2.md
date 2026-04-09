Fluxos transacionais

# Fluxos transacionais

Para utilizar esta solução você deve [ativar](https://connect-stone.stone.com.br/docs/opera%C3%A7%C3%B5es) a **integração** e **escolher** o **fluxo transacional** utilizado pelo POS.

## Fluxos Transacionais

Existem dois tipos de fluxos transacionais pré-definidos para realizar pagamento via POS:

**1. Listagem de pedidos:**\
Essa integração permite que o parceiro crie uma lista de pedidos a serem pagas no POS.

Nesse fluxo, o POS integrado irá listar todos os pedidos em aberto para esse terminal nessa conta no Pagar.me.

Dessa forma, o operador do POS poderá selecionar um pedido para realizar seu pagamento.

![](https://files.readme.io/cb4de8f-list_orders.png "list_orders.png")

**2. Pagamento direto de pedidos:**\
Essa integração permite que o parceiro crie um pedido a ser pago diretamente para um POS integrado.

Nesse fluxo, o POS fica em espera, aguardando a criação de uma cobrança direcionada a ele.

Ao receber um pedido, o POS entra automaticamente na tela de pagamento para realizar a transação.

O fluxo ao realizar pagamento ou cancelamento de transações é idêntica à _1. Listagem de pedidos:_

![](https://files.readme.io/12444c3-direct_payment.png "direct_payment.png")
