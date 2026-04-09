package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentTerminal;
import dev.kalles.sale.payment.domain.TerminalOperationMode;

import java.util.List;
import java.util.Optional;

public interface PaymentTerminalRepository {

    Optional<PaymentTerminal> findById(String terminalId);

    Optional<PaymentTerminal> findByPointIdAndOperationMode(String pointId, TerminalOperationMode operationMode);

    void saveAll(List<PaymentTerminal> terminals);

    void save(PaymentTerminal terminal);
}
