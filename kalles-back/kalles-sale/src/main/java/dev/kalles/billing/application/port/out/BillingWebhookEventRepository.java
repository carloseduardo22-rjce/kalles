package dev.kalles.billing.application.port.out;

import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingWebhookEvent;

public interface BillingWebhookEventRepository {

    boolean existsByProviderAndExternalEventId(BillingProvider provider, String externalEventId);

    BillingWebhookEvent save(BillingWebhookEvent event);
}
