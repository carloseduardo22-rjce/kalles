package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentCommand;
import dev.kalles.sale.payment.domain.PaymentFlow;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Map;

public record ProcessPaymentRequest(
        @NotNull(message = "provider is required")
        PaymentProvider provider,

        @NotNull(message = "flow is required")
        PaymentFlow flow,

        @NotBlank(message = "externalReference is required")
        String externalReference,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "targetId is required")
        String targetId,

        String idempotencyKey,
        String description,
        PaymentMethodType methodType,
        Map<String, Object> metadata
) {

    public PaymentCommand toCommand() {
        return new PaymentCommand(
                provider,
                flow,
                externalReference,
                amount,
                targetId,
                idempotencyKey,
                description,
                methodType,
                metadata
        );
    }
}
