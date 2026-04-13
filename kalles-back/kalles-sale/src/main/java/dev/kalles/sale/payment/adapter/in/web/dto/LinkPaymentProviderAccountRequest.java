package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.application.port.in.command.LinkPaymentProviderAccountCommand;
import dev.kalles.sale.payment.domain.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record LinkPaymentProviderAccountRequest(
        @NotNull(message = "provider is required")
        PaymentProvider provider,

        @NotBlank(message = "authorizationCode is required")
        String authorizationCode,

        @NotBlank(message = "state is required")
        String state,

        Map<String, Object> metadata
) {

    public LinkPaymentProviderAccountCommand toCommand(UUID tenantId) {
        return new LinkPaymentProviderAccountCommand(provider, authorizationCode, tenantId, metadata);
    }
}
