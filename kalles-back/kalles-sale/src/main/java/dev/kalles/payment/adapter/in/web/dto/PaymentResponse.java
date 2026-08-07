package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;

import java.util.Map;

public record PaymentResponse(
        PaymentProvider provider,
        String providerOrderId,
        String providerPaymentId,
        PaymentStatus status,
        Map<String, Object> metadata
) {

    public static PaymentResponse from(PaymentProvider provider, PaymentResult result) {
        return new PaymentResponse(
                provider,
                result.providerOrderId(),
                result.providerPaymentId(),
                result.status(),
                result.metadata()
        );
    }
}
