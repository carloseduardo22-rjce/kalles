package dev.kalles.billing.adapter.out.persistence.repository;

import dev.kalles.billing.adapter.out.persistence.entity.BillingWebhookEventEntity;
import dev.kalles.billing.domain.BillingProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataBillingWebhookEventRepository extends JpaRepository<BillingWebhookEventEntity, UUID> {

    boolean existsByProviderAndExternalEventId(BillingProvider provider, String externalEventId);
}
