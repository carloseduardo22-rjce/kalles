package dev.kalles.payment.application.port.in.command;

import dev.kalles.payment.domain.PaymentProvider;

import java.util.Map;
import java.util.Objects;

public record ActivatePaymentTerminalCommand(
        PaymentProvider provider,
        String storeId,
        String pointId,
        String terminalSerial,
        Map<String, Object> metadata
) {

    public ActivatePaymentTerminalCommand {
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(storeId, "storeId is required");
        Objects.requireNonNull(pointId, "pointId is required");
        Objects.requireNonNull(terminalSerial, "terminalSerial is required");
        metadata = CommandMetadata.immutableCopy(metadata);
    }
}
