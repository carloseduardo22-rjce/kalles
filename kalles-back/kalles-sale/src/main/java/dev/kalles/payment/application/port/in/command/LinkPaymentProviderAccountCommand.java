package dev.kalles.payment.application.port.in.command;

import dev.kalles.payment.domain.PaymentProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record LinkPaymentProviderAccountCommand(
        PaymentProvider provider,
        String authorizationCode,
        UUID tenantId,
        Map<String, Object> metadata
) {

    public LinkPaymentProviderAccountCommand {
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(authorizationCode, "authorizationCode is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        metadata = CommandMetadata.immutableCopy(metadata);
    }
}
