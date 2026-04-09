package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record ClosePaymentOrderRequest(
        @NotNull(message = "status is required")
        PaymentStatus status
) {
}
