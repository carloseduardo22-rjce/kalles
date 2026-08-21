package dev.kalles.sale.dto;

import dev.kalles.sale.enums.PaymentMethod;

import java.math.BigDecimal;

public record SessionPaymentMethodTotal(PaymentMethod method, BigDecimal total) {}
