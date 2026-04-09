package dev.kalles.sale.payment.adapter.out.mercadopago;

public class MercadoPagoAdapterException extends RuntimeException {

    public MercadoPagoAdapterException(String message) {
        super(message);
    }

    public MercadoPagoAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
