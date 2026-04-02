package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Terminal;

import java.util.List;
import java.util.Optional;

public interface TerminalRepository {
    Optional<Terminal> findById(String terminalId);
    Optional<Terminal> findByPosIdAndOperationMode(String posId, String operationMode);

    void saveAll(List<Terminal> terminals);

    void save(Terminal terminal);
}
