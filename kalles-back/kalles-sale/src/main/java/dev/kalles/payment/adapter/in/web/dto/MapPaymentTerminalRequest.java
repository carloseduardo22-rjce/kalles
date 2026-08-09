package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.application.port.in.command.MapPaymentTerminalCommand;
import dev.kalles.payment.domain.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MapPaymentTerminalRequest(
        @NotNull(message = "cashRegisterId is required")
        UUID cashRegisterId,

        @NotNull(message = "provider is required")
        PaymentProvider provider,

        @NotBlank(message = "terminalSerial is required")
        String terminalSerial
) {

    public MapPaymentTerminalCommand toCommand() {
        return new MapPaymentTerminalCommand(cashRegisterId, provider, terminalSerial);
    }
}
