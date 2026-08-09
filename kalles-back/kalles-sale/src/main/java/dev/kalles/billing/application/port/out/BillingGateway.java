package dev.kalles.billing.application.port.out;

import dev.kalles.billing.domain.BillingInterval;
import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingStatus;

import java.time.Instant;
import java.util.UUID;

public interface BillingGateway {

    CheckoutSession createSubscriptionCheckout(CheckoutCommand command);

    PortalSession createPortalSession(PortalCommand command);

    WebhookNotification parseWebhook(String payload, String signature);

    record CheckoutCommand(
            UUID tenantId,
            String customerId,
            String customerEmail,
            String customerName,
            String priceId,
            String returnUrl
    ) {}

    record CheckoutSession(
            String sessionId,
            String clientSecret,
            String url,
            String customerId,
            String subscriptionId,
            BillingStatus status
    ) {}

    record PortalCommand(
            String customerId,
            String returnUrl
    ) {}

    record PortalSession(
            String url
    ) {}

    record WebhookNotification(
            String eventId,
            String eventType,
            BillingProvider provider,
            UUID tenantId,
            String customerId,
            String subscriptionId,
            String checkoutSessionId,
            String productId,
            String priceId,
            BillingStatus status,
            BillingInterval interval,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            boolean cancelAtPeriodEnd
    ) {}
}
