package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStatus;

public interface ClosePaymentOrderUseCase {

    void execute(PaymentProvider provider, String providerOrderId, PaymentStatus status);
}
