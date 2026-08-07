package dev.kalles.core.strategy;

import java.math.BigDecimal;

import dev.kalles.core.enums.payment.PaymentMethod;

public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    PaymentResult process(BigDecimal amount);
}
