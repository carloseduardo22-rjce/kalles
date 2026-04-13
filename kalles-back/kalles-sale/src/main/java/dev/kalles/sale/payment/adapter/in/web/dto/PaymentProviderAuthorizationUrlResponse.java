package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentProvider;

public record PaymentProviderAuthorizationUrlResponse(
        PaymentProvider provider,
        String authorizationUrl
) {
}
