package dev.kalles.payment.application.port.in.command;

import dev.kalles.payment.domain.PaymentProvider;

import java.util.Objects;

public record ListPaymentTerminalsQuery(
        PaymentProvider provider,
        String storeId,
        String pointId
) {

    public ListPaymentTerminalsQuery {
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(storeId, "storeId is required");
        Objects.requireNonNull(pointId, "pointId is required");
    }
}
