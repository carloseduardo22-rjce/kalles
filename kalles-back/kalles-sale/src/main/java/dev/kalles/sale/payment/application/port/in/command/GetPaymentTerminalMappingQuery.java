package dev.kalles.sale.payment.application.port.in.command;

import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.UUID;

public record GetPaymentTerminalMappingQuery(
        UUID cashRegisterId,
        PaymentProvider provider
) {
}
