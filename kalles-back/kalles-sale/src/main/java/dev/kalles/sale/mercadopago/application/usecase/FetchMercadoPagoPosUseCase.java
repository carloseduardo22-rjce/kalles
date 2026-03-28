package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.port.MercadoPagoPosPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FetchMercadoPagoPosUseCase {

    private final MercadoPagoPosPort mercadoPagoPosPort;

    public FetchMercadoPagoPosUseCase(MercadoPagoPosPort mercadoPagoPosPort) {
        this.mercadoPagoPosPort = mercadoPagoPosPort;
    }

    public List<Map<String, Object>> execute() {
        return mercadoPagoPosPort.fetchPos();
    }
}
