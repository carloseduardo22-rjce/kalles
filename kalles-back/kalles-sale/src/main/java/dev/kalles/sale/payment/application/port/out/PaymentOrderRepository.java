package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentOrder;
import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.Optional;

public interface PaymentOrderRepository {

    void save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByProviderOrderIdAndProvider(String providerOrderId, PaymentProvider provider);
}
