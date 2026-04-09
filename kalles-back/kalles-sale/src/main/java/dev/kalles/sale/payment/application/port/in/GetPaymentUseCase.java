package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentResult;

public interface GetPaymentUseCase {

    PaymentResult execute(PaymentProvider provider, String providerOrderId);
}
