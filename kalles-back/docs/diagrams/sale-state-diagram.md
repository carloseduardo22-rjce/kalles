# Diagrama de Estados — Sale (Venda)

> Representa todas as transições possíveis de estado de uma `Sale` no sistema PDV Kalles.

```mermaid
stateDiagram-v2
    [*] --> OPEN : createForSession()

    OPEN --> PAYMENT_IN_PROGRESS : startPayment()
    OPEN --> ON_HOLD : hold()
    OPEN --> CANCELED : cancel()

    PAYMENT_IN_PROGRESS --> PAID : finishPayment()<br/>amountDue == 0
    PAYMENT_IN_PROGRESS --> CANCELED : cancel()
    PAYMENT_IN_PROGRESS --> OPEN : resume()

    PAID --> COMPLETED : completeSale()

    ON_HOLD --> OPEN : resume()
    ON_HOLD --> CANCELED : cancel()

    COMPLETED --> [*]
    CANCELED --> [*]

    note right of OPEN
        Operações permitidas:
        • addItem()
        • removeItem()
        • startPayment()
        • hold()
        • cancel()
    end note

    note right of PAYMENT_IN_PROGRESS
        Operações permitidas:
        • finishPayment() (automático)
        • cancel()
        • resume() → volta para OPEN
    end note

    note right of PAID
        Única operação:
        • completeSale()
    end note

    note left of ON_HOLD
        Operações permitidas:
        • addItem()
        • removeItem()
        • resume()
        • cancel()
    end note

    note right of COMPLETED
        Estado terminal.
        Nenhuma operação permitida.
    end note

    note left of CANCELED
        Estado terminal.
        Nenhuma operação permitida.
    end note
```

## Regras de Negócio por Transição

| Transição                  | Trigger           | Regra                                                  |
| -------------------------- | ----------------- | ------------------------------------------------------ |
| OPEN → PAYMENT_IN_PROGRESS | `startPayment()`  | Inicia o pagamento, calcula `amountDue = total`        |
| PAYMENT_IN_PROGRESS → PAID | `finishPayment()` | Automático quando `amountDue == 0` após `addPayment()` |
| PAID → COMPLETED           | `completeSale()`  | BR009: `amountDue` deve ser ≤ 0                        |
| OPEN → ON_HOLD             | `hold()`          | Venda pausada pelo operador                            |
| ON_HOLD → OPEN             | `resume()`        | Venda retomada                                         |
| \* → CANCELED              | `cancel()`        | Requer permissão (SUPERVISOR+) ou autorização          |

## Fluxo Típico (Happy Path)

```
OPEN → (addItem) → OPEN → (startPayment) → PAYMENT_IN_PROGRESS
     → (addPayment até amountDue=0) → PAID → (completeSale) → COMPLETED
```
