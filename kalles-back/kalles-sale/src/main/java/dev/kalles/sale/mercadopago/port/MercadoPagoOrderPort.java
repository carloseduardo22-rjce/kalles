package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;

public interface MercadoPagoOrderPort {
    ResultadoQr createOrder(CobrancaQr cobrancaQr);
}
