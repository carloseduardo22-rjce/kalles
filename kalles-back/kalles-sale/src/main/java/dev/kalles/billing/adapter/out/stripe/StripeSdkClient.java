package dev.kalles.billing.adapter.out.stripe;

import dev.kalles.billing.domain.BillingInterval;
import dev.kalles.billing.domain.BillingStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface StripeSdkClient {

    StripeCustomer createCustomer(String email, String name, Map<String, String> metadata);

    StripeCheckoutSession createSubscriptionCheckout(StripeCheckoutRequest request);

    StripePortalSession createPortalSession(String customerId, String returnUrl, String configurationId);

    StripeWebhookPayload parseWebhook(String payload, String signature, String webhookSecret);

    StripeSubscription retrieveSubscription(String subscriptionId);

    record StripeCustomer(
            String id,
            String email,
            String name
    ) {}

    record StripeCheckoutRequest(
            UUID tenantId,
            String customerId,
            String customerEmail,
            String customerName,
            String priceId,
            String returnUrl
    ) {}

    record StripeCheckoutSession(
            String id,
            String clientSecret,
            String url,
            String customerId,
            String subscriptionId,
            BillingStatus status
    ) {}

    record StripePortalSession(
            String url
    ) {}

    record StripeWebhookPayload(
            String eventId,
            String eventType,
            UUID tenantId,
            String customerId,
            String subscriptionId,
            String checkoutSessionId,
            String priceId,
            String productId,
            BillingStatus status,
            BillingInterval interval,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            boolean cancelAtPeriodEnd
    ) {}

    record StripeSubscription(
            String id,
            String customerId,
            String priceId,
            String productId,
            BillingStatus status,
            BillingInterval interval,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            boolean cancelAtPeriodEnd,
            Map<String, String> metadata
    ) {}
}
