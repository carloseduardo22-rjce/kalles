package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;

import java.util.Optional;
import java.util.UUID;

public interface PaymentStoreRepository {

    Optional<PaymentStore> findById(UUID id);

    Optional<PaymentStore> findByExternalReferenceAndProvider(String externalReference, PaymentProvider provider);

    Optional<PaymentStore> findByCompanyIdAndProvider(UUID companyId, PaymentProvider provider);

    void save(PaymentStore store);

    void updateProviderStoreId(UUID id, String providerStoreId);
}
