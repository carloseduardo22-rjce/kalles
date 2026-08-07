package dev.kalles.payment.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record PaymentCommand(
        PaymentProvider provider,
        PaymentFlow flow,
        String externalReference,
        BigDecimal amount,
        String targetId,
        String idempotencyKey,
        String description,
        PaymentMethodType methodType,
        Map<String, Object> metadata
) {

    public PaymentCommand {
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(flow, "flow is required");
        Objects.requireNonNull(externalReference, "externalReference is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(targetId, "targetId is required");
        methodType = methodType == null ? PaymentMethodType.UNSPECIFIED : methodType;
        metadata = DomainMetadata.immutableCopy(metadata);
    }
}
