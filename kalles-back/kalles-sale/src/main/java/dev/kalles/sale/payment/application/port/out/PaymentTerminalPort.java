package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentTerminal;
import dev.kalles.sale.payment.domain.TerminalOperationMode;

import java.util.List;

public interface PaymentTerminalPort extends ProviderAwarePort {

    List<PaymentTerminal> listTerminals(String storeId, String pointId);

    boolean changeOperationMode(String terminalId, TerminalOperationMode operationMode);
}
