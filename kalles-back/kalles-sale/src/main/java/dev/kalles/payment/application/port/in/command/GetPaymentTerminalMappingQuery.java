package dev.kalles.payment.application.port.in.command;

import dev.kalles.payment.domain.PaymentProvider;

import java.util.UUID;

public record GetPaymentTerminalMappingQuery(
        UUID cashRegisterId,
        PaymentProvider provider
) {
}
