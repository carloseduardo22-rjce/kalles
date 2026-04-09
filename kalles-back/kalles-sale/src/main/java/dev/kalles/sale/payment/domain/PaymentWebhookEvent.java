package dev.kalles.sale.payment.domain;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentWebhookEvent(
        PaymentProvider provider,
        String action,
        String providerOrderId,
        String providerPaymentId,
        String externalReference,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethodType methodType,
        Map<String, Object> metadata
) {

    public PaymentWebhookEvent {
        status = status == null ? PaymentStatus.UNKNOWN : status;
        methodType = methodType == null ? PaymentMethodType.UNSPECIFIED : methodType;
        metadata = DomainMetadata.immutableCopy(metadata);
    }
}
