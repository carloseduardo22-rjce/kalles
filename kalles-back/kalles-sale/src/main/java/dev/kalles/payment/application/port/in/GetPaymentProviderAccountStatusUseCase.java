package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;

public interface GetPaymentProviderAccountStatusUseCase {

    boolean execute(PaymentProvider provider);
}
