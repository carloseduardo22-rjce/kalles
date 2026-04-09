package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentTerminal;
import dev.kalles.sale.payment.domain.TerminalOperationMode;

public record PaymentTerminalResponse(
        String id,
        String pointId,
        String storeId,
        String externalPointId,
        TerminalOperationMode operationMode
) {

    public static PaymentTerminalResponse from(PaymentTerminal terminal) {
        return new PaymentTerminalResponse(
                terminal.id(),
                terminal.pointId(),
                terminal.storeId(),
                terminal.externalPointId(),
                terminal.operationMode()
        );
    }
}
