package dev.kalles.payment.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record PaymentOrder(
        PaymentProvider provider,
        String providerOrderId,
        String providerPaymentId,
        PaymentStatus status,
        String externalReference,
        BigDecimal amount,
        String idempotencyKey,
        PaymentFlow flow,
        PaymentMethodType methodType
) {

    public PaymentOrder {
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(providerOrderId, "providerOrderId is required");
        Objects.requireNonNull(externalReference, "externalReference is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(flow, "flow is required");
        status = status == null ? PaymentStatus.UNKNOWN : status;
        methodType = methodType == null ? PaymentMethodType.UNSPECIFIED : methodType;
    }

    public PaymentOrder withStatus(PaymentStatus newStatus) {
        return new PaymentOrder(
                provider,
                providerOrderId,
                providerPaymentId,
                newStatus,
                externalReference,
                amount,
                idempotencyKey,
                flow,
                methodType
        );
    }

    public PaymentOrder withProviderPaymentId(String newProviderPaymentId) {
        return new PaymentOrder(
                provider,
                providerOrderId,
                newProviderPaymentId,
                status,
                externalReference,
                amount,
                idempotencyKey,
                flow,
                methodType
        );
    }
}
