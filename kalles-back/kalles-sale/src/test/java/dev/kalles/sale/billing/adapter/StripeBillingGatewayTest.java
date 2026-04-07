package dev.kalles.sale.billing.adapter;

import dev.kalles.sale.billing.adapter.out.stripe.StripeBillingGateway;
import dev.kalles.sale.billing.adapter.out.stripe.StripeSdkClient;
import dev.kalles.sale.billing.application.port.out.BillingGateway;
import dev.kalles.sale.billing.application.service.StripeBillingProperties;
import dev.kalles.sale.billing.domain.BillingInterval;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StripeBillingGatewayTest {

    private StripeSdkClient stripeSdkClient;
    private StripeBillingGateway gateway;

    @BeforeEach
    void setUp() {
        stripeSdkClient = mock(StripeSdkClient.class);
        gateway = new StripeBillingGateway(
                stripeSdkClient,
                new StripeBillingProperties(
                        "sk_test",
                        "pk_test",
                        "whsec_test",
                        "price_monthly",
                        "bpc_123",
                        "default-monthly"
                )
        );
    }

    @Test
    void shouldCreateStripeCustomerBeforeOpeningCheckoutWhenCustomerDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        when(stripeSdkClient.createCustomer(any(), any(), any()))
                .thenReturn(new StripeSdkClient.StripeCustomer("cus_123", "billing@kalles.dev", "Kalles Admin"));
        when(stripeSdkClient.createSubscriptionCheckout(any()))
                .thenReturn(new StripeSdkClient.StripeCheckoutSession(
                        "cs_123",
                        "cs_secret_123",
                        null,
                        "cus_123",
                        "sub_123",
                        BillingStatus.CHECKOUT_CREATED
                ));

        BillingGateway.CheckoutSession result = gateway.createSubscriptionCheckout(
                new BillingGateway.CheckoutCommand(
                        tenantId,
                        null,
                        "billing@kalles.dev",
                        "Kalles Admin",
                        "price_monthly",
                        "https://app.kalles.dev/billing/return"
                )
        );

        assertEquals("cs_123", result.sessionId());
        assertEquals("cus_123", result.customerId());
        assertEquals("sub_123", result.subscriptionId());
        verify(stripeSdkClient).createCustomer(any(), any(), any());
        verify(stripeSdkClient).createSubscriptionCheckout(any());
    }

    @Test
    void shouldCreatePortalSessionUsingConfiguredPortalId() {
        when(stripeSdkClient.createPortalSession("cus_123", "https://app.kalles.dev/account", "bpc_123"))
                .thenReturn(new StripeSdkClient.StripePortalSession("https://billing.stripe.com/session"));

        BillingGateway.PortalSession result = gateway.createPortalSession(
                new BillingGateway.PortalCommand("cus_123", "https://app.kalles.dev/account")
        );

        assertEquals("https://billing.stripe.com/session", result.url());
    }

    @Test
    void shouldMapWebhookPayloadToGenericNotification() {
        UUID tenantId = UUID.randomUUID();
        Instant periodStart = Instant.parse("2026-04-01T00:00:00Z");
        Instant periodEnd = Instant.parse("2026-05-01T00:00:00Z");

        when(stripeSdkClient.parseWebhook("{payload}", "signature", "whsec_test"))
                .thenReturn(new StripeSdkClient.StripeWebhookPayload(
                        "evt_123",
                        "customer.subscription.updated",
                        tenantId,
                        "cus_123",
                        "sub_123",
                        null,
                        "price_monthly",
                        "prod_monthly",
                        BillingStatus.ACTIVE,
                        BillingInterval.MONTHLY,
                        periodStart,
                        periodEnd,
                        false
                ));

        BillingGateway.WebhookNotification notification = gateway.parseWebhook("{payload}", "signature");

        assertEquals("evt_123", notification.eventId());
        assertEquals(BillingProvider.STRIPE, notification.provider());
        assertEquals(tenantId, notification.tenantId());
        assertEquals("cus_123", notification.customerId());
        assertEquals("sub_123", notification.subscriptionId());
        assertEquals(BillingStatus.ACTIVE, notification.status());
        assertEquals(BillingInterval.MONTHLY, notification.interval());
        assertEquals(periodStart, notification.currentPeriodStart());
        assertEquals(periodEnd, notification.currentPeriodEnd());
        assertNull(notification.checkoutSessionId());
    }
}
