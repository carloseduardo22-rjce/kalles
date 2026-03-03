package dev.kalles.sale.core.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import dev.kalles.sale.core.enums.payment.PaymentMethod;

@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public PaymentResult process(BigDecimal amount) {
        return PaymentResult.confirmed("Cash payment received successfully.");
    }
}
