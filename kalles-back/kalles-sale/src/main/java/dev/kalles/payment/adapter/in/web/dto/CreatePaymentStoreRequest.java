package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.payment.domain.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreatePaymentStoreRequest(
        @NotNull(message = "provider is required")
        PaymentProvider provider,

        @NotBlank(message = "externalReference is required")
        String externalReference,

        @NotNull(message = "companyId is required")
        UUID companyId,

        Map<String, Object> metadata
) {

    public CreatePaymentStoreCommand toCommand() {
        return new CreatePaymentStoreCommand(provider, companyId, externalReference, metadata);
    }
}
