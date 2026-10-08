package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.exception.PaymentProviderIntegrationException;

public class MercadoPagoAdapterException extends PaymentProviderIntegrationException {

    public MercadoPagoAdapterException(String message) {
        super(message);
    }

    public MercadoPagoAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
