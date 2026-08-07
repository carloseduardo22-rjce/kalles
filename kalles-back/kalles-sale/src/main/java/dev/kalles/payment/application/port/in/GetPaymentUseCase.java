package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;

public interface GetPaymentUseCase {

    PaymentResult execute(PaymentProvider provider, String providerOrderId);
}
