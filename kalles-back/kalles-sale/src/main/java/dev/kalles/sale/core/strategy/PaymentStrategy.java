package dev.kalles.sale.core.strategy;

import java.math.BigDecimal;

import dev.kalles.sale.core.enums.payment.PaymentMethod;

public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    PaymentResult process(BigDecimal amount);
}
