package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;

import java.util.Optional;

public interface GetPaymentStoreStatusUseCase {

    Optional<PaymentStore> findByExternalReference(PaymentProvider provider, String externalReference);

    Optional<PaymentStore> findCurrentTenant(PaymentProvider provider);
}
