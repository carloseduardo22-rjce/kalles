package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStatus;

public interface ClosePaymentOrderUseCase {

    void execute(PaymentProvider provider, String providerOrderId, PaymentStatus status);
}
