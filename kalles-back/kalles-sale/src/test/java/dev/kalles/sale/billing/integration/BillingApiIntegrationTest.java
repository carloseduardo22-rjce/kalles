package dev.kalles.sale.billing.integration;

import dev.kalles.sale.billing.application.port.out.BillingGateway;
import dev.kalles.sale.billing.domain.BillingInterval;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import dev.kalles.sale.billing.support.AbstractBillingApiSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("integration")
class BillingApiIntegrationTest extends AbstractBillingApiSupport {

    @BeforeEach
    void setUp() {
        resetBillingScenario();
    }

    @Test
    void shouldCreateCheckoutSessionForAuthenticatedTenant() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        givenAuthenticated(auth)
                .body(java.util.Map.of("returnUrl", "https://app.kalles.dev/billing/return"))
                .when()
                .post("/api/billing/checkout-sessions")
                .then()
                .statusCode(200)
                .body("sessionId", equalTo("cs_test_123"))
                .body("status", equalTo("CHECKOUT_CREATED"));

        assertThat(stubBillingGateway.lastCheckoutCommand()).isNotNull();
        assertThat(stubBillingGateway.lastCheckoutCommand().tenantId()).isEqualTo(TENANT_ID);
        assertThat(stubBillingGateway.lastCheckoutCommand().customerEmail()).isEqualTo(TENANT_ADMIN_EMAIL);
        assertThat(billingSubscriptionRepository.findByTenantIdAndProvider(TENANT_ID, dev.kalles.sale.billing.domain.BillingProvider.STRIPE)).isPresent();
    }

    @Test
    void shouldCreatePortalSessionForTenantWithExistingStripeCustomer() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedSubscription(TENANT_ID, "cus_seed_123");

        givenAuthenticated(auth)
                .body(java.util.Map.of("returnUrl", "https://app.kalles.dev/account"))
                .when()
                .post("/api/billing/portal-sessions")
                .then()
                .statusCode(200)
                .body("url", equalTo("https://billing.stripe.test/portal-session"));

        assertThat(stubBillingGateway.lastPortalCommand()).isNotNull();
        assertThat(stubBillingGateway.lastPortalCommand().customerId()).isEqualTo("cus_seed_123");
    }

    @Test
    void shouldReturnConflictWhenPortalSessionIsRequestedWithoutSubscription() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        givenAuthenticated(auth)
                .body(java.util.Map.of("returnUrl", "https://app.kalles.dev/account"))
                .when()
                .post("/api/billing/portal-sessions")
                .then()
                .statusCode(409)
                .body("detail", equalTo("Nenhuma assinatura Stripe encontrada para este tenant."));
    }

    @Test
    void shouldAcceptWebhookWithoutAuthenticationAndPersistSubscriptionState() {
        stubBillingGateway.setNextWebhookNotification(new BillingGateway.WebhookNotification(
                "evt_checkout_completed_123",
                "customer.subscription.updated",
                BillingProvider.STRIPE,
                TENANT_ID,
                "cus_webhook_123",
                "sub_webhook_123",
                "cs_webhook_123",
                "prod_monthly",
                "price_monthly",
                BillingStatus.ACTIVE,
                BillingInterval.MONTHLY,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z"),
                false
        ));

        givenWebhookRequest()
                .header("Stripe-Signature", "sig_test_123")
                .body("{\"id\":\"evt_checkout_completed_123\"}")
                .when()
                .post("/api/billing/webhook")
                .then()
                .statusCode(200);

        var persistedSubscription = billingSubscriptionRepository.findByTenantIdAndProvider(TENANT_ID, BillingProvider.STRIPE);

        assertThat(persistedSubscription).isPresent();
        assertThat(persistedSubscription.get().getExternalCustomerId()).isEqualTo("cus_webhook_123");
        assertThat(persistedSubscription.get().getExternalSubscriptionId()).isEqualTo("sub_webhook_123");
        assertThat(persistedSubscription.get().getStatus()).isEqualTo(BillingStatus.ACTIVE);
        assertThat(billingWebhookEventRepository.existsByProviderAndExternalEventId(
                BillingProvider.STRIPE,
                "evt_checkout_completed_123"
        )).isTrue();
    }

    @Test
    void shouldIgnoreDuplicateWebhookEventIdempotently() {
        stubBillingGateway.setNextWebhookNotification(new BillingGateway.WebhookNotification(
                "evt_duplicate_123",
                "customer.subscription.updated",
                BillingProvider.STRIPE,
                TENANT_ID,
                "cus_duplicate_123",
                "sub_duplicate_123",
                null,
                "prod_monthly",
                "price_monthly",
                BillingStatus.ACTIVE,
                BillingInterval.MONTHLY,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z"),
                false
        ));

        givenWebhookRequest()
                .header("Stripe-Signature", "sig_test_123")
                .body("{\"id\":\"evt_duplicate_123\"}")
                .when()
                .post("/api/billing/webhook")
                .then()
                .statusCode(200);

        UUID subscriptionIdAfterFirstCall = billingSubscriptionRepository
                .findByTenantIdAndProvider(TENANT_ID, BillingProvider.STRIPE)
                .orElseThrow()
                .getId();

        stubBillingGateway.setNextWebhookNotification(new BillingGateway.WebhookNotification(
                "evt_duplicate_123",
                "customer.subscription.updated",
                BillingProvider.STRIPE,
                TENANT_ID,
                "cus_changed_ignored",
                "sub_changed_ignored",
                null,
                "prod_monthly",
                "price_monthly",
                BillingStatus.CANCELED,
                BillingInterval.MONTHLY,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z"),
                true
        ));

        givenWebhookRequest()
                .header("Stripe-Signature", "sig_test_123")
                .body("{\"id\":\"evt_duplicate_123\"}")
                .when()
                .post("/api/billing/webhook")
                .then()
                .statusCode(200);

        var persistedSubscription = billingSubscriptionRepository
                .findByTenantIdAndProvider(TENANT_ID, BillingProvider.STRIPE)
                .orElseThrow();

        assertThat(persistedSubscription.getId()).isEqualTo(subscriptionIdAfterFirstCall);
        assertThat(persistedSubscription.getExternalCustomerId()).isEqualTo("cus_duplicate_123");
        assertThat(persistedSubscription.getStatus()).isEqualTo(BillingStatus.ACTIVE);
        assertThat(billingWebhookEventRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldResolveTenantUsingExistingCustomerWhenWebhookHasNoTenantMetadata() {
        seedSubscription(TENANT_ID, "cus_seed_123");

        stubBillingGateway.setNextWebhookNotification(new BillingGateway.WebhookNotification(
                "evt_existing_customer_123",
                "invoice.payment_failed",
                BillingProvider.STRIPE,
                null,
                "cus_seed_123",
                "sub_seed_123",
                null,
                "prod_monthly",
                "price_monthly",
                BillingStatus.PAST_DUE,
                BillingInterval.MONTHLY,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z"),
                false
        ));

        givenWebhookRequest()
                .header("Stripe-Signature", "sig_test_existing_customer")
                .body("{\"id\":\"evt_existing_customer_123\"}")
                .when()
                .post("/api/billing/webhook")
                .then()
                .statusCode(200);

        var persistedSubscription = billingSubscriptionRepository
                .findByTenantIdAndProvider(TENANT_ID, BillingProvider.STRIPE)
                .orElseThrow();

        assertThat(persistedSubscription.getExternalCustomerId()).isEqualTo("cus_seed_123");
        assertThat(persistedSubscription.getExternalSubscriptionId()).isEqualTo("sub_seed_123");
        assertThat(persistedSubscription.getStatus()).isEqualTo(BillingStatus.PAST_DUE);
        assertThat(persistedSubscription.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void shouldReturnConflictWhenWebhookCannotResolveTenant() {
        stubBillingGateway.setNextWebhookNotification(new BillingGateway.WebhookNotification(
                "evt_unknown_tenant_123",
                "checkout.session.completed",
                BillingProvider.STRIPE,
                null,
                "cus_unknown_123",
                null,
                "cs_unknown_123",
                "prod_monthly",
                "price_monthly",
                BillingStatus.CHECKOUT_CREATED,
                BillingInterval.MONTHLY,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z"),
                false
        ));

        givenWebhookRequest()
                .header("Stripe-Signature", "sig_unknown_tenant")
                .body("{\"id\":\"evt_unknown_tenant_123\"}")
                .when()
                .post("/api/billing/webhook")
                .then()
                .statusCode(409)
                .body("detail", equalTo("Nao foi possivel resolver o tenant da notificacao Stripe."));

        assertThat(billingSubscriptionRepository.count()).isZero();
        assertThat(billingWebhookEventRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadGatewayWhenWebhookValidationFails() {
        stubBillingGateway.failNextWebhook("Falha ao validar webhook Stripe.");

        givenWebhookRequest()
                .header("Stripe-Signature", "sig_invalid")
                .body("{\"id\":\"evt_invalid_signature\"}")
                .when()
                .post("/api/billing/webhook")
                .then()
                .statusCode(502)
                .body("detail", equalTo("Falha ao validar webhook Stripe."));

        assertThat(billingSubscriptionRepository.count()).isZero();
        assertThat(billingWebhookEventRepository.count()).isZero();
    }
}
