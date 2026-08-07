package dev.kalles.payment.application.port.in.command;

import dev.kalles.payment.domain.PaymentProvider;

import java.util.UUID;

public record MapPaymentTerminalCommand(
        UUID cashRegisterId,
        PaymentProvider provider,
        String terminalSerial
) {
}
