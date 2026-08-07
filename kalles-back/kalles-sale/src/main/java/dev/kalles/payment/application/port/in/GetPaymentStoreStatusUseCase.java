package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;

import java.util.Optional;

public interface GetPaymentStoreStatusUseCase {

    Optional<PaymentStore> findByExternalReference(PaymentProvider provider, String externalReference);

    Optional<PaymentStore> findCurrentTenant(PaymentProvider provider);
}
