package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;

public interface GetPaymentProviderAccountStatusUseCase {

    boolean execute(PaymentProvider provider);
}
