package dev.kalles.sale.payment.application.port.in.command;

import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.UUID;

public record MapPaymentTerminalCommand(
        UUID cashRegisterId,
        PaymentProvider provider,
        String terminalSerial
) {
}
