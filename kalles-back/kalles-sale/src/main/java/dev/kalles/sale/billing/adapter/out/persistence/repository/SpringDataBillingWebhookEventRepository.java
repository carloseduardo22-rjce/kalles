package dev.kalles.sale.billing.adapter.out.persistence.repository;

import dev.kalles.sale.billing.adapter.out.persistence.entity.BillingWebhookEventEntity;
import dev.kalles.sale.billing.domain.BillingProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataBillingWebhookEventRepository extends JpaRepository<BillingWebhookEventEntity, UUID> {

    boolean existsByProviderAndExternalEventId(BillingProvider provider, String externalEventId);
}
