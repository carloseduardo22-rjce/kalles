package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentProvider;

public interface ProviderAwarePort {

    PaymentProvider provider();
}
