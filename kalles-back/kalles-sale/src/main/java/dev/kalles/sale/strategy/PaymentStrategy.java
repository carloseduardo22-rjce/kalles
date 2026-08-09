package dev.kalles.sale.strategy;

import dev.kalles.sale.enums.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    PaymentResult process(BigDecimal amount);
}
