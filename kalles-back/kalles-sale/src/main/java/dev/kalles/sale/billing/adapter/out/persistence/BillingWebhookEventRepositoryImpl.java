package dev.kalles.sale.billing.adapter.out.persistence;

import dev.kalles.sale.billing.adapter.out.persistence.entity.BillingWebhookEventEntity;
import dev.kalles.sale.billing.adapter.out.persistence.repository.SpringDataBillingWebhookEventRepository;
import dev.kalles.sale.billing.application.port.out.BillingWebhookEventRepository;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingWebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BillingWebhookEventRepositoryImpl implements BillingWebhookEventRepository {

    private final SpringDataBillingWebhookEventRepository repository;

    @Override
    public boolean existsByProviderAndExternalEventId(BillingProvider provider, String externalEventId) {
        return repository.existsByProviderAndExternalEventId(provider, externalEventId);
    }

    @Override
    public BillingWebhookEvent save(BillingWebhookEvent event) {
        BillingWebhookEventEntity entity = new BillingWebhookEventEntity();
        entity.setId(event.getId());
        entity.setProvider(event.getProvider());
        entity.setExternalEventId(event.getExternalEventId());
        entity.setEventType(event.getEventType());

        BillingWebhookEventEntity saved = repository.save(entity);
        return new BillingWebhookEvent(saved.getId(), saved.getProvider(), saved.getExternalEventId(), saved.getEventType());
    }
}
