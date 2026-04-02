package dev.kalles.sale.mercadopago.port;

import java.util.List;
import java.util.UUID;

import dev.kalles.sale.mercadopago.domain.Terminal;

public interface MercadoPagoTerminalPort {
    List<Terminal> fetchTerminals(UUID storeId, UUID posId);
    boolean changeToPdvMode(String terminalId);
}