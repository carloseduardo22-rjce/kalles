package dev.kalles.sale.mercadopago.exception;

public class MercadoPagoIntegrationException extends RuntimeException {

    public MercadoPagoIntegrationException(String message) {
        super(message);
    }

    public MercadoPagoIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
