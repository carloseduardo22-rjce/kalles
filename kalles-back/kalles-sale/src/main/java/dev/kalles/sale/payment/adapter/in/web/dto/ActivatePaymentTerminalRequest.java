package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.application.port.in.command.ActivatePaymentTerminalCommand;
import dev.kalles.sale.payment.domain.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ActivatePaymentTerminalRequest(
        @NotNull(message = "provider is required")
        PaymentProvider provider,

        @NotBlank(message = "storeId is required")
        String storeId,

        @NotBlank(message = "pointId is required")
        String pointId,

        @NotBlank(message = "terminalSerial is required")
        String terminalSerial,

        Map<String, Object> metadata
) {

    public ActivatePaymentTerminalCommand toCommand() {
        return new ActivatePaymentTerminalCommand(provider, storeId, pointId, terminalSerial, metadata);
    }
}
