package dev.kalles.payment.domain;

import java.util.Objects;

public record PaymentTerminal(
        String id,
        String pointId,
        String storeId,
        String externalPointId,
        TerminalOperationMode operationMode
) {

    public PaymentTerminal {
        Objects.requireNonNull(id, "id is required");
        operationMode = operationMode == null ? TerminalOperationMode.UNKNOWN : operationMode;
    }

    public PaymentTerminal withOperationMode(TerminalOperationMode newOperationMode) {
        return new PaymentTerminal(id, pointId, storeId, externalPointId, newOperationMode);
    }
}
