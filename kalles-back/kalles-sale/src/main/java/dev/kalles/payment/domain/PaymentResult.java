package dev.kalles.payment.domain;

import java.util.Map;

public record PaymentResult(
        String providerOrderId,
        String providerPaymentId,
        PaymentStatus status,
        Map<String, Object> metadata
) {

    public PaymentResult {
        status = status == null ? PaymentStatus.UNKNOWN : status;
        metadata = DomainMetadata.immutableCopy(metadata);
    }
}
