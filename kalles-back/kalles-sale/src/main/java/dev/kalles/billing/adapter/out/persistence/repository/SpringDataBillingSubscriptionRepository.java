package dev.kalles.billing.adapter.out.persistence.repository;

import dev.kalles.billing.adapter.out.persistence.entity.BillingSubscriptionEntity;
import dev.kalles.billing.domain.BillingProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataBillingSubscriptionRepository extends JpaRepository<BillingSubscriptionEntity, UUID> {

    Optional<BillingSubscriptionEntity> findByTenantIdAndProvider(UUID tenantId, BillingProvider provider);

    Optional<BillingSubscriptionEntity> findByExternalSubscriptionId(String externalSubscriptionId);

    Optional<BillingSubscriptionEntity> findByExternalCustomerId(String externalCustomerId);
}
