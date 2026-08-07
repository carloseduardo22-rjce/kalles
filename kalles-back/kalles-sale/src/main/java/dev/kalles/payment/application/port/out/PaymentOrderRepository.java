package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentOrder;
import dev.kalles.payment.domain.PaymentProvider;

import java.util.Optional;

public interface PaymentOrderRepository {

    void save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByProviderOrderIdAndProvider(String providerOrderId, PaymentProvider provider);
}
