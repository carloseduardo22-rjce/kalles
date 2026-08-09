package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentTerminal;
import dev.kalles.payment.domain.TerminalOperationMode;

import java.util.List;
import java.util.Optional;

public interface PaymentTerminalRepository {

    Optional<PaymentTerminal> findById(String terminalId);

    Optional<PaymentTerminal> findByPointIdAndOperationMode(String pointId, TerminalOperationMode operationMode);

    void saveAll(List<PaymentTerminal> terminals);

    void save(PaymentTerminal terminal);
}
