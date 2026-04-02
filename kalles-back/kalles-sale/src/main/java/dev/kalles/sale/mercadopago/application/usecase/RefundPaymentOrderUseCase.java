package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.springframework.stereotype.Service;

@Service
public class RefundPaymentOrderUseCase {

    private final MercadoPagoOrderPort orderPort;

    public RefundPaymentOrderUseCase(MercadoPagoOrderPort orderPort) {
        this.orderPort = orderPort;
    }

    public void execute(String paymentId) {
        orderPort.refundOrderPoint(paymentId);
    }
}
