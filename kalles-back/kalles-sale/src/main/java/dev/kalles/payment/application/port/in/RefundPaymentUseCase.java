package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;

public interface RefundPaymentUseCase {

    void execute(PaymentProvider provider, String providerPaymentId);
}
