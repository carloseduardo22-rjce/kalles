package dev.kalles.sale.payment.application.port.in.command;

import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CreatePaymentStoreCommand(
        PaymentProvider provider,
        UUID companyId,
        String externalReference,
        Map<String, Object> metadata
) {

    public CreatePaymentStoreCommand {
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(externalReference, "externalReference is required");
        metadata = CommandMetadata.immutableCopy(metadata);
    }
}
