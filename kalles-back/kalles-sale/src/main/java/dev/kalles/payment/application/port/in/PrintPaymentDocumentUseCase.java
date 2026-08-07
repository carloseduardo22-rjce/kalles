package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.payment.domain.PaymentProvider;

public interface PrintPaymentDocumentUseCase {

    void execute(PaymentProvider provider, String providerOrderId, PaymentDocumentPrintCommand command);
}
