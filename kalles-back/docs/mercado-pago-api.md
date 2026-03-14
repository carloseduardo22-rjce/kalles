Markdown
# Referência da API Mercado Pago - Integração PDV (QR Dinâmico)

Este documento contém os payloads e endpoints estritos para a integração de pagamentos presenciais via Mercado Pago.
**REGRAS ARQUITETURAIS PARA A IA:**
1. Use estritamente os campos descritos nestes JSONs para criar os Records/DTOs.
2. Não adicione campos de "cash_out" ou "extra-cash". O escopo é apenas "payments".
3. O modo do QR Code será SEMPRE `dynamic`.
4. O `fixed_amount` no POS será SEMPRE `false`.

---

## 1. SETUP: Criar Loja (Store)
Cria o estabelecimento físico no Mercado Pago. O `id` retornado deve ser salvo no banco de dados do ERP.

* **Endpoint:** `POST https://api.mercadopago.com/users/{USER_ID}/stores`
* **Headers:** `Authorization: Bearer {ACCESS_TOKEN}`, `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Nome da Loja no ERP",
  "external_id": "ID_LOJA_ERP_001",
  "location": {
    "street_number": "123",
    "street_name": "Rua Exemplo",
    "city_name": "São Paulo",
    "state_name": "SP",
    "latitude": -23.550520,
    "longitude": -46.633308,
    "reference": "Perto do centro"
  }
}
Response (Sucesso):

JSON
{
  "id": 1234567,
  "name": "Nome da Loja no ERP",
  "external_id": "ID_LOJA_ERP_001",
  "location": { ... }
}
2. SETUP: Criar Caixa (POS)
Vincula um terminal (PDV) do ERP à Loja criada no passo anterior.

Endpoint: POST https://api.mercadopago.com/pos

Headers: Authorization: Bearer {ACCESS_TOKEN}, Content-Type: application/json

Request Body:

JSON
{
  "name": "Caixa 01",
  "fixed_amount": false,
  "store_id": 1234567,
  "external_store_id": "ID_LOJA_ERP_001",
  "external_id": "ID_CAIXA_ERP_001"
}
Response (Sucesso):

JSON
{
  "id": 2711382,
  "user_id": 446566691,
  "name": "Caixa 01",
  "fixed_amount": false,
  "store_id": 1234567,
  "external_store_id": "ID_LOJA_ERP_001",
  "external_id": "ID_CAIXA_ERP_001"
}
3. TRANSAÇÃO: Criar Order (QR Dinâmico)
Gera a intenção de pagamento. A resposta contém a string EMVCo (qr_data) que o frontend do ERP transformará em imagem.

Endpoint: POST https://api.mercadopago.com/v1/orders

Headers: * Authorization: Bearer {ACCESS_TOKEN}

Content-Type: application/json

X-Idempotency-Key: {UUID_GERADO_PELO_ERP}

Request Body:

JSON
{
  "type": "qr",
  "total_amount": 50.00,
  "external_reference": "PEDIDO_ERP_9999",
  "config": {
    "qr": {
      "external_pos_id": "ID_CAIXA_ERP_001",
      "mode": "dynamic"
    }
  },
  "transactions": {
    "payments": [
      {
        "amount": 50.00
      }
    ]
  }
}
Response (Sucesso):
Atenção para extrair o type_response.qr_data e o id da Order.

JSON
{
  "id": "ORD01K372G4J4FXZ9HGHZMJMGGPKE",
  "type": "qr",
  "external_reference": "PEDIDO_ERP_9999",
  "total_amount": "50.00",
  "status": "created",
  "transactions": {
    "payments": [
      {
        "id": "PAY01K372G4J4FXZ9HGHZMKWSQS20",
        "amount": "50.00",
        "status": "created"
      }
    ]
  },
  "config": {
    "qr": {
      "external_pos_id": "ID_CAIXA_ERP_001",
      "mode": "dynamic"
    }
  },
  "type_response": {
    "qr_data": "00020101021226580014br.gov.bcb.qr01368ee55a9c-7db3..."
  }
}
4. OPERAÇÕES: Cancelar Order
Só pode ser feito se o status da order for created.

Endpoint: POST https://api.mercadopago.com/v1/orders/{ORDER_ID}/cancel

Headers: Authorization: Bearer {ACCESS_TOKEN}, X-Idempotency-Key: {UUID}

Response (Sucesso):

JSON
{
  "id": "ORD01...",
  "status": "canceled"
}
5. OPERAÇÕES: Reembolsar Order (Refund)
Só pode ser feito se o status da order for processed (paga).

Endpoint: POST https://api.mercadopago.com/v1/orders/{ORDER_ID}/refund

Headers: Authorization: Bearer {ACCESS_TOKEN}, X-Idempotency-Key: {UUID}

Response (Sucesso):
O Mercado Pago cria um nó refunds.

JSON
{
  "id": "ORD01...",
  "status": "processed",
  "transactions": {
    "refunds": [
      {
        "id": "REF01...",
        "amount": "50.00",
        "status": "processing"
      }
    ]
  }
}
6. OPERAÇÕES: Consultar Order
Usado para verificar o status de um pagamento.

Endpoint: GET https://api.mercadopago.com/v1/orders/{ORDER_ID}

Headers: Authorization: Bearer {ACCESS_TOKEN}

Response:
Retorna o mesmo objeto JSON da criação da Order, mas com o campo status atualizado (ex: processed, closed, expired).

***

Pronto! Com este arquivo salvo no seu projeto, você tem o "dicionário" perfeito.

Agora você já pode enviar aquele **"Mega Prompt de Integração Mercado Pago"** (que criamos na mensagem anterior) para o Claude Code / Copilot.

Gostaria que eu aguardasse você executar a ETAPA 1 lá no Copilot para revisarmos jun