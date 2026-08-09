package dev.kalles.sale.dto;

import java.math.BigDecimal;

import dev.kalles.sale.enums.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
    @Schema(description = "Método de pagamento", allowableValues = {"CASH", "PIX", "CREDIT_CARD", "DEBIT_CARD"}, example = "CASH")
    @NotNull PaymentMethod method,

    @Schema(description = "Valor do pagamento em reais. Para CASH, pode ser maior que o total — o troco é calculado automaticamente.", example = "50.00")
    @NotNull @Positive @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount
) {}
