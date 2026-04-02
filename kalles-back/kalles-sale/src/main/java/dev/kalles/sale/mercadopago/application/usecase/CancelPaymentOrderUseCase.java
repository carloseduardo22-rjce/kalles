package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.springframework.stereotype.Service;

@Service
public class CancelPaymentOrderUseCase {

    private final MercadoPagoOrderPort orderPort;

    public CancelPaymentOrderUseCase(MercadoPagoOrderPort orderPort) {
        this.orderPort = orderPort;
    }

    public void execute(String orderId) {
        orderPort.cancelOrderPoint(orderId);
    }
}
