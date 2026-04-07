package dev.kalles.sale.billing.application.port.out;

import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingSubscription;

import java.util.Optional;
import java.util.UUID;

public interface BillingSubscriptionRepository {

    Optional<BillingSubscription> findByTenantIdAndProvider(UUID tenantId, BillingProvider provider);

    Optional<BillingSubscription> findByExternalSubscriptionId(String externalSubscriptionId);

    Optional<BillingSubscription> findByExternalCustomerId(String externalCustomerId);

    BillingSubscription save(BillingSubscription subscription);
}
