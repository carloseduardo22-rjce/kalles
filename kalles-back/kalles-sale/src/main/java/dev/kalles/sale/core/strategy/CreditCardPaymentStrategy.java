package dev.kalles.sale.core.strategy;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.kalles.sale.core.enums.payment.PaymentMethod;

@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentResult process(BigDecimal amount) {
        String simulatedTransactionId = UUID.randomUUID().toString();
        return PaymentResult.confirmed(simulatedTransactionId, "Credit card payment processed successfully (simulated).");
    }
}
