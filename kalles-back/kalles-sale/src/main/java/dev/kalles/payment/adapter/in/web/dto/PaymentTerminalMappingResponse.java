package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentTerminalMapping;

import java.util.UUID;

public record PaymentTerminalMappingResponse(
        UUID id,
        UUID cashRegisterId,
        PaymentProvider provider,
        String terminalSerial,
        boolean active
) {

    public static PaymentTerminalMappingResponse from(PaymentTerminalMapping mapping) {
        return new PaymentTerminalMappingResponse(
                mapping.id(),
                mapping.cashRegisterId(),
                mapping.provider(),
                mapping.terminalSerial(),
                mapping.active()
        );
    }
}
