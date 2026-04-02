package dev.kalles.sale.mercadopago.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.kalles.sale.mercadopago.domain.Terminal;
import dev.kalles.sale.mercadopago.port.MercadoPagoTerminalPort;

@Service
public class FetchMercadoPagoTerminalsUseCase {

    private final MercadoPagoTerminalPort mercadoPagoTerminalPort;

    public FetchMercadoPagoTerminalsUseCase(MercadoPagoTerminalPort mercadoPagoTerminalPort) {
        this.mercadoPagoTerminalPort = mercadoPagoTerminalPort;
    }

    public List<Terminal> execute(UUID storeId, UUID posId) {
        return mercadoPagoTerminalPort.fetchTerminals(storeId, posId);
    }
}