package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentTerminal;
import dev.kalles.payment.domain.TerminalOperationMode;

import java.util.List;

public interface PaymentTerminalPort extends ProviderAwarePort {

    List<PaymentTerminal> listTerminals(String storeId, String pointId);

    boolean changeOperationMode(String terminalId, TerminalOperationMode operationMode);
}
