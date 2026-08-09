package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.application.port.in.command.CreatePaymentPointCommand;
import dev.kalles.payment.domain.PaymentProvider;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreatePaymentPointRequest(
        @NotNull(message = "provider is required")
        PaymentProvider provider,

        @NotNull(message = "cashRegisterId is required")
        UUID cashRegisterId,

        String externalReference,
        Map<String, Object> metadata
) {

    public CreatePaymentPointCommand toCommand() {
        String resolvedExternalReference = externalReference != null && !externalReference.isBlank()
                ? externalReference
                : cashRegisterId.toString();

        return new CreatePaymentPointCommand(provider, cashRegisterId, resolvedExternalReference, metadata);
    }
}
