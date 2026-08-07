package dev.kalles.billing.steps;

import dev.kalles.billing.application.port.out.BillingGateway;
import dev.kalles.billing.domain.BillingInterval;
import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingStatus;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BillingSteps extends BillingCucumberSpringConfiguration {

    private AuthContext authContext;
    private Response response;

    @Before
    public void beforeScenario() {
        resetBillingScenario();
        authContext = null;
        response = null;
    }

    @Dado("um admin autenticado para billing")
    public void givenAuthenticatedTenantAdmin() {
        authContext = authenticateTenantAdminWithCsrf();
    }

    @Dado("uma assinatura Stripe existente para o tenant atual")
    public void givenExistingStripeSubscription() {
        seedSubscription(TENANT_ID, "cus_seed_123");
    }

    @Dado("um webhook Stripe valido para o tenant atual")
    public void givenValidWebhookForCurrentTenant() {
        stubBillingGateway.setNextWebhookNotification(new BillingGateway.WebhookNotification(
                "evt_bdd_valid_123",
                "customer.subscription.updated",
                BillingProvider.STRIPE,
                TENANT_ID,
                "cus_bdd_123",
                "sub_bdd_123",
                "cs_bdd_123",
                "prod_monthly",
                "price_monthly",
                BillingStatus.ACTIVE,
                BillingInterval.MONTHLY,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z"),
                false
        ));
    }

    @Dado("um webhook Stripe sem dados suficientes para resolver o tenant")
    public void givenWebhookWithoutTenantResolution() {
        stubBillingGateway.setNextWebhookNotification(new BillingGateway.WebhookNotification(
                "evt_bdd_unknown_tenant_123",
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
    }

    @Quando("ele solicitar uma checkout session de billing")
    public void whenCreatingBillingCheckoutSession() {
        response = givenAuthenticated(authContext)
                .body(Map.of("returnUrl", "https://app.kalles.dev/billing/return"))
                .when()
                .post("/api/billing/checkout-sessions");
    }

    @Quando("ele solicitar uma portal session de billing")
    public void whenCreatingBillingPortalSession() {
        response = givenAuthenticated(authContext)
                .body(Map.of("returnUrl", "https://app.kalles.dev/account"))
                .when()
                .post("/api/billing/portal-sessions");
    }

    @Quando("a Stripe enviar o webhook de billing")
    public void whenPostingBillingWebhook() {
        response = givenWebhookRequest()
                .header("Stripe-Signature", "sig_bdd_123")
                .body("{\"id\":\"evt_bdd\"}")
                .when()
                .post("/api/billing/webhook");
    }

    @Entao("a resposta de billing deve ter status HTTP {int}")
    public void thenBillingResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a checkout session deve ser criada para o tenant atual")
    public void thenCheckoutSessionShouldBeCreatedForCurrentTenant() {
        assertThat(stubBillingGateway.lastCheckoutCommand()).isNotNull();
        assertThat(stubBillingGateway.lastCheckoutCommand().tenantId()).isEqualTo(TENANT_ID);
        assertThat(response.jsonPath().getString("sessionId")).isEqualTo("cs_test_123");
    }

    @Entao("a resposta deve informar que nao existe assinatura Stripe")
    public void thenBillingShouldInformMissingSubscription() {
        assertThat(response.jsonPath().getString("detail"))
                .isEqualTo("Nenhuma assinatura Stripe encontrada para este tenant.");
    }

    @Entao("a assinatura do tenant deve ser persistida pelo webhook")
    public void thenWebhookShouldPersistTenantSubscription() {
        var persistedSubscription = billingSubscriptionRepository
                .findByTenantIdAndProvider(TENANT_ID, BillingProvider.STRIPE);

        assertThat(persistedSubscription).isPresent();
        assertThat(persistedSubscription.get().getExternalCustomerId()).isEqualTo("cus_bdd_123");
        assertThat(persistedSubscription.get().getStatus()).isEqualTo(BillingStatus.ACTIVE);
        assertThat(billingWebhookEventRepository.existsByProviderAndExternalEventId(
                BillingProvider.STRIPE,
                "evt_bdd_valid_123"
        )).isTrue();
    }

    @Entao("a resposta deve informar que o tenant do webhook nao pode ser resolvido")
    public void thenWebhookShouldInformTenantResolutionFailure() {
        assertThat(response.jsonPath().getString("detail"))
                .isEqualTo("Nao foi possivel resolver o tenant da notificacao Stripe.");
        assertThat(billingSubscriptionRepository.count()).isZero();
    }
}
