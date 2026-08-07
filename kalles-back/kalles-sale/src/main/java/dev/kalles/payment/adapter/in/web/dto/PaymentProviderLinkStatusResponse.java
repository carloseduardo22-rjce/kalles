package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentProvider;

public record PaymentProviderLinkStatusResponse(
        PaymentProvider provider,
        boolean linked
) {
}
