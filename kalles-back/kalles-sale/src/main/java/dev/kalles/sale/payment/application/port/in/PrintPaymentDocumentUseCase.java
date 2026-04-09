package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.sale.payment.domain.PaymentProvider;

public interface PrintPaymentDocumentUseCase {

    void execute(PaymentProvider provider, String providerOrderId, PaymentDocumentPrintCommand command);
}
