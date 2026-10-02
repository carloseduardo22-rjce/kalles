package dev.kalles.payment.exception;

import dev.kalles.shared.exception.ConflictException;

public class PaymentStoreReferenceMismatchException extends ConflictException {

    public PaymentStoreReferenceMismatchException() {
        super("PAYMENT_STORE_REFERENCE_MISMATCH", "A filial ja tem loja no provedor de pagamento com outra referencia externa.");
    }
}
