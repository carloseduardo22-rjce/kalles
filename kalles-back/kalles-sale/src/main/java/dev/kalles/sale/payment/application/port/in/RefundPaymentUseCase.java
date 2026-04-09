package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;

public interface RefundPaymentUseCase {

    void execute(PaymentProvider provider, String providerPaymentId);
}
