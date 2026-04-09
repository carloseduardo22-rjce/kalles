package dev.kalles.sale.payment.application.port.in.command;

import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.Map;
import java.util.Objects;

public record LinkPaymentProviderAccountCommand(
        PaymentProvider provider,
        String authorizationCode,
        String state,
        Map<String, Object> metadata
) {

    public LinkPaymentProviderAccountCommand {
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(authorizationCode, "authorizationCode is required");
        Objects.requireNonNull(state, "state is required");
        metadata = CommandMetadata.immutableCopy(metadata);
    }
}
