package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentProvider;

public interface ProviderAwarePort {

    PaymentProvider provider();
}
