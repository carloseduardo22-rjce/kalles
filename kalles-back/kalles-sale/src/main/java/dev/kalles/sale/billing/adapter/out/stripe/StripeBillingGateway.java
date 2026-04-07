package dev.kalles.sale.billing.adapter.out.stripe;

import dev.kalles.sale.billing.application.port.out.BillingGateway;
import dev.kalles.sale.billing.application.service.StripeBillingProperties;
import dev.kalles.sale.billing.domain.BillingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StripeBillingGateway implements BillingGateway {

    private final StripeSdkClient stripeSdkClient;
    private final StripeBillingProperties stripeBillingProperties;

    @Override
    public CheckoutSession createSubscriptionCheckout(CheckoutCommand command) {
        String customerId = command.customerId();

        if (customerId == null || customerId.isBlank()) {
            StripeSdkClient.StripeCustomer customer = stripeSdkClient.createCustomer(
                    command.customerEmail(),
                    command.customerName(),
                    Map.of("tenantId", command.tenantId().toString())
            );
            customerId = customer.id();
        }

        StripeSdkClient.StripeCheckoutSession checkoutSession = stripeSdkClient.createSubscriptionCheckout(
                new StripeSdkClient.StripeCheckoutRequest(
                        command.tenantId(),
                        customerId,
                        command.customerEmail(),
                        command.customerName(),
                        command.priceId(),
                        command.returnUrl()
                )
        );

        return new CheckoutSession(
                checkoutSession.id(),
                checkoutSession.clientSecret(),
                checkoutSession.url(),
                checkoutSession.customerId(),
                checkoutSession.subscriptionId(),
                checkoutSession.status()
        );
    }

    @Override
    public PortalSession createPortalSession(PortalCommand command) {
        StripeSdkClient.StripePortalSession portalSession = stripeSdkClient.createPortalSession(
                command.customerId(),
                command.returnUrl(),
                stripeBillingProperties.portalConfigurationId()
        );

        return new PortalSession(portalSession.url());
    }

    @Override
    public WebhookNotification parseWebhook(String payload, String signature) {
        StripeSdkClient.StripeWebhookPayload webhookPayload = stripeSdkClient.parseWebhook(
                payload,
                signature,
                stripeBillingProperties.webhookSecret()
        );

        return new WebhookNotification(
                webhookPayload.eventId(),
                webhookPayload.eventType(),
                BillingProvider.STRIPE,
                webhookPayload.tenantId(),
                webhookPayload.customerId(),
                webhookPayload.subscriptionId(),
                webhookPayload.checkoutSessionId(),
                webhookPayload.productId(),
                webhookPayload.priceId(),
                webhookPayload.status(),
                webhookPayload.interval(),
                webhookPayload.currentPeriodStart(),
                webhookPayload.currentPeriodEnd(),
                webhookPayload.cancelAtPeriodEnd()
        );
    }
}
