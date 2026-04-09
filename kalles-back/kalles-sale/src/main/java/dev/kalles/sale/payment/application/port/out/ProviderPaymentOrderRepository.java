package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentOrder;
import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.Optional;

public interface ProviderPaymentOrderRepository extends ProviderAwarePort {

    void save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByProviderOrderId(String providerOrderId);

    default Optional<PaymentOrder> findByProviderOrderIdAndProvider(String providerOrderId, PaymentProvider provider) {
        if (provider != provider()) {
            return Optional.empty();
        }
        return findByProviderOrderId(providerOrderId);
    }
}
