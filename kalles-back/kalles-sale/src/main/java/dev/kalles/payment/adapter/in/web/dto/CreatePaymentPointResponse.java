package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentProvider;

public record CreatePaymentPointResponse(
        PaymentProvider provider,
        String providerPointId
) {

    public static CreatePaymentPointResponse from(PaymentPoint point) {
        return new CreatePaymentPointResponse(point.provider(), point.providerPointId());
    }
}
