package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.ResultadoPoint;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.springframework.stereotype.Service;

@Service
public class FetchPaymentOrderUseCase {

    private final MercadoPagoOrderPort orderPort;

    public FetchPaymentOrderUseCase(MercadoPagoOrderPort orderPort) {
        this.orderPort = orderPort;
    }

    public ResultadoPoint execute(String orderId) {
        return orderPort.getOrderPoint(orderId);
    }
}
