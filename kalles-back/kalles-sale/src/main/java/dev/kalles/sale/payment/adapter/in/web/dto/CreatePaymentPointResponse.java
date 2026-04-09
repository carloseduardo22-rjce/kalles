package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentPoint;
import dev.kalles.sale.payment.domain.PaymentProvider;

public record CreatePaymentPointResponse(
        PaymentProvider provider,
        String providerPointId
) {

    public static CreatePaymentPointResponse from(PaymentPoint point) {
        return new CreatePaymentPointResponse(point.provider(), point.providerPointId());
    }
}
