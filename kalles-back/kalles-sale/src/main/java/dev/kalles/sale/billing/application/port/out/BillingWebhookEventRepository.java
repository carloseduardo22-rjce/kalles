package dev.kalles.sale.billing.application.port.out;

import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingWebhookEvent;

public interface BillingWebhookEventRepository {

    boolean existsByProviderAndExternalEventId(BillingProvider provider, String externalEventId);

    BillingWebhookEvent save(BillingWebhookEvent event);
}
