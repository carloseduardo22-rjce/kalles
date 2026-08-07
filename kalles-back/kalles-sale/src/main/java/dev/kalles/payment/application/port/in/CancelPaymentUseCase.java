package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;

public interface CancelPaymentUseCase {

    void execute(PaymentProvider provider, String providerOrderId);
}
