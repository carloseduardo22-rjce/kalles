package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.CobrancaPoint;
import dev.kalles.sale.mercadopago.domain.ResultadoPoint;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;

public interface MercadoPagoOrderPort {
    ResultadoQr createOrder(CobrancaQr cobrancaQr);
    
    // Mercado Pago Point Integration
    ResultadoPoint createOrderPoint(CobrancaPoint cobrancaPoint);
    void cancelOrderPoint(String orderId);
    ResultadoPoint getOrderPoint(String orderId);
    void refundOrderPoint(String paymentId);
}
