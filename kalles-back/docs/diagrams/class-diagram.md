# Diagrama de Classes — Kalles Sale (PDV)

> Visão geral das entidades, serviços, patterns e seus relacionamentos.

## 1. Entidades de Domínio (Core)

```mermaid
classDiagram
    direction TB

    class Sale {
        -UUID id
        -String sessionToken
        -SaleState state
        -List~SaleItem~ items
        -List~ItemRemovedSale~ itemsRemoved
        -List~SaleCancellation~ cancellations
        -List~Payment~ payments
        -BigDecimal subtotal
        -BigDecimal total
        -BigDecimal amountDue
        +addItem(Product)
        +removeItem(Product, Operator, Operator)
        +startPayment()
        +finishPayment()
        +addPayment(Payment)
        +cancel(Operator, Operator)
        +hold()
        +resume()
        +completeSale()
        +createForSession(String)$ Sale
        +getStateName() String
    }

    class SaleItem {
        -UUID id
        -Sale sale
        -Product product
        -int quantity
        -BigDecimal unitPrice
        +incrementQuantity()
        +getSubtotal() BigDecimal
    }

    class Payment {
        -UUID id
        -Sale sale
        -PaymentMethod method
        -BigDecimal amount
        -BigDecimal changeAmount
        -String transactionId
        -boolean confirmed
        -LocalDateTime createdAt
    }

    class Product {
        -UUID id
        -String name
        -String internalCode
        -String barcode
        -String description
        -BigDecimal price
        -boolean active
    }

    class ItemRemovedSale {
        -UUID id
        -Sale sale
        -Product product
        -Integer quantityRemoved
        -Operator requestedBy
        -Operator authorizedBy
        -LocalDateTime timestamp
    }

    class SaleCancellation {
        -UUID id
        -Sale sale
        -Operator requestedBy
        -Operator authorizedBy
        -LocalDateTime canceledAt
    }

    class Operator {
        -UUID id
        -String name
        -String code
        -PermissionLevel permissionLevel
    }

    Sale "1" *-- "*" SaleItem : items
    Sale "1" *-- "*" Payment : payments
    Sale "1" *-- "*" ItemRemovedSale : itemsRemoved
    Sale "1" *-- "*" SaleCancellation : cancellations
    SaleItem "*" --> "1" Product : product
    ItemRemovedSale "*" --> "1" Product : product
    ItemRemovedSale "*" --> "1" Operator : requestedBy
    ItemRemovedSale "*" --> "0..1" Operator : authorizedBy
    SaleCancellation "*" --> "1" Operator : requestedBy
    SaleCancellation "*" --> "0..1" Operator : authorizedBy
```

## 2. State Pattern (Estado da Venda)

```mermaid
classDiagram
    direction TB

    class SaleState {
        <<interface>>
        +getName() String
        +getDescription() String
        +addItem(Sale, Product)
        +removeItem(Sale, Product, Operator, Operator)
        +startPayment(Sale)
        +finishPayment(Sale)
        +cancel(Sale)
        +hold(Sale)
        +resume(Sale)
        +completeSale(Sale)
    }

    class AbstractSaleState {
        <<abstract>>
        +addItem() ⛔ throws
        +removeItem() ⛔ throws
        +startPayment() ⛔ throws
        +finishPayment() ⛔ throws
        +cancel() ⛔ throws
        +hold() ⛔ throws
        +resume() ⛔ throws
        +completeSale() ⛔ throws
    }

    class OpenState {
        +NAME = "OPEN"
        +addItem() ✅
        +removeItem() ✅
        +startPayment() ✅
        +hold() ✅
        +cancel() ✅
    }

    class PaymentInProgressState {
        +NAME = "PAYMENT_IN_PROGRESS"
        +finishPayment() ✅
        +cancel() ✅
        +resume() ✅
    }

    class PaidState {
        +NAME = "PAID"
        +completeSale() ✅
    }

    class OnHoldState {
        +NAME = "ON_HOLD"
        +addItem() ✅
        +removeItem() ✅
        +resume() ✅
        +cancel() ✅
    }

    class CompletedState {
        +NAME = "COMPLETED"
        ~estado terminal~
    }

    class CanceledState {
        +NAME = "CANCELED"
        ~estado terminal~
    }

    SaleState <|.. AbstractSaleState
    AbstractSaleState <|-- OpenState
    AbstractSaleState <|-- PaymentInProgressState
    AbstractSaleState <|-- PaidState
    AbstractSaleState <|-- OnHoldState
    AbstractSaleState <|-- CompletedState
    AbstractSaleState <|-- CanceledState

    Sale --> SaleState : state
```

## 3. Strategy Pattern (Pagamento)

```mermaid
classDiagram
    direction LR

    class PaymentStrategy {
        <<interface>>
        +getPaymentMethod() PaymentMethod
        +process(BigDecimal) PaymentResult
    }

    class PaymentResult {
        <<record>>
        +boolean confirmed
        +String transactionId
        +String message
        +confirmed(String)$ PaymentResult
        +confirmed(String, String)$ PaymentResult
        +pending(String, String)$ PaymentResult
    }

    class PaymentFactory {
        -Map~PaymentMethod, PaymentStrategy~ strategies
        +getStrategy(PaymentMethod) PaymentStrategy
    }

    class CashPaymentStrategy {
        +getPaymentMethod() CASH
        +process(amount) PaymentResult
    }

    class PixPaymentStrategy {
        +getPaymentMethod() PIX
        +process(amount) PaymentResult
    }

    class CreditCardPaymentStrategy {
        +getPaymentMethod() CREDIT_CARD
        +process(amount) PaymentResult
    }

    class DebitCardPaymentStrategy {
        +getPaymentMethod() DEBIT_CARD
        +process(amount) PaymentResult
    }

    class PaymentMethod {
        <<enum>>
        CASH
        PIX
        CREDIT_CARD
        DEBIT_CARD
    }

    PaymentStrategy <|.. CashPaymentStrategy
    PaymentStrategy <|.. PixPaymentStrategy
    PaymentStrategy <|.. CreditCardPaymentStrategy
    PaymentStrategy <|.. DebitCardPaymentStrategy
    PaymentFactory --> PaymentStrategy : resolve
    PaymentStrategy --> PaymentResult : returns
    PaymentStrategy --> PaymentMethod : identifies
```

## 4. Camada de Serviço

```mermaid
classDiagram
    direction TB

    class SaleService {
        -SaleRepository saleRepository
        -ProductRepository productRepository
        -CheckoutSessionService checkoutSessionService
        -OperatorRepository operatorRepository
        -PermissionService permissionService
        +addItemByInternalCode(sessionToken, code) Sale
        +addItemByBarCode(sessionToken, barcode) Sale
        +removeItemByInternalCode(sessionToken, code, operatorId) Sale
        +removeItemByBarCode(sessionToken, barcode, operatorId) Sale
        +removeItemByInternalCodeWithAuthorization(...) Sale
        +removeItemByBarCodeWithAuthorization(...) Sale
        +cancelSale(sessionToken, operatorId) Sale
        +cancelSaleWithAuthorization(...) Sale
        +completeSale(sessionToken) Sale
        +searchProducts(description) List~Product~
    }

    class PaymentService {
        -SaleRepository saleRepository
        -PaymentFactory paymentFactory
        -CheckoutSessionService checkoutSessionService
        +addPayment(sessionToken, method, amount) Sale
    }

    class PermissionService {
        +canRemoveItens(Operator) boolean
        +canAuthorizeRemoval(Operator, Operator) boolean
        +canCancelSale(Operator) boolean
        +canAuthorizeCancellation(Operator, Operator) boolean
    }

    class CheckoutSessionService {
        <<interface>>
        +findByToken(String) Optional~Session~
        +isSessionOpen(String) boolean
        +getOpenSessionOrThrow(String) Session
    }

    class SaleController {
        -SaleService saleService
        -PaymentService paymentService
        +addItem(sessionToken, request)
        +removeItem(sessionToken, productCode, ...)
        +cancelSale(sessionToken, operatorId, ...)
        +addPayment(sessionToken, request)
        +completeSale(sessionToken)
    }

    SaleController --> SaleService
    SaleController --> PaymentService
    SaleService --> PermissionService
    SaleService --> CheckoutSessionService
    PaymentService --> CheckoutSessionService
    PaymentService --> PaymentFactory
```

## 5. Cash Register (Caixa Registradora)

```mermaid
classDiagram
    direction TB

    class CashRegister {
        -UUID id
        -String code
        -String description
        -boolean active
        +activate()
        +deactivate()
    }

    class CashRegisterSession {
        -UUID id
        -CashRegister cashRegister
        -Operator operator
        -InitialAmount initialAmount
        -SessionPeriod sessionPeriod
        -SessionStatus status
        +open(CashRegister, Operator, BigDecimal)$ CashRegisterSession
        +isOpen() boolean
        +close()
    }

    class InitialAmount {
        <<Embeddable>>
        -BigDecimal value
    }

    class SessionPeriod {
        <<Embeddable>>
        -LocalDateTime openedAt
        -LocalDateTime closedAt
        +isOpen() boolean
        +close(LocalDateTime)
    }

    class SessionStatus {
        <<enum>>
        OPEN
        CLOSED
    }

    CashRegisterSession "*" --> "1" CashRegister
    CashRegisterSession "*" --> "1" Operator
    CashRegisterSession *-- InitialAmount
    CashRegisterSession *-- SessionPeriod
    CashRegisterSession --> SessionStatus
```

## 6. Enums

```mermaid
classDiagram
    class PaymentMethod {
        <<enum>>
        CASH
        PIX
        CREDIT_CARD
        DEBIT_CARD
    }

    class PermissionLevel {
        <<enum>>
        BASIC(1)
        SUPERVISOR(2)
        MANAGER(3)
        +getLevel() int
        +canAuthorize(PermissionLevel) boolean
    }

    class ProductCodeType {
        <<enum>>
        INTERNAL_CODE
        BAR_CODE
    }
```
