package dev.kalles.billing.support;

import dev.kalles.billing.adapter.out.persistence.entity.BillingSubscriptionEntity;
import dev.kalles.billing.adapter.out.persistence.repository.SpringDataBillingWebhookEventRepository;
import dev.kalles.billing.adapter.out.persistence.repository.SpringDataBillingSubscriptionRepository;
import dev.kalles.billing.domain.BillingInterval;
import dev.kalles.billing.domain.BillingProvider;
import dev.kalles.billing.domain.BillingStatus;
import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = dev.kalles.KallesSaleApplication.class)
@Import(BillingTestConfiguration.class)
public abstract class AbstractBillingApiSupport extends AbstractCompanyContextApiSupport {

    @Autowired
    protected SpringDataBillingSubscriptionRepository billingSubscriptionRepository;

    @Autowired
    protected SpringDataBillingWebhookEventRepository billingWebhookEventRepository;

    @Autowired
    protected BillingTestConfiguration.StubBillingGateway stubBillingGateway;

    @DynamicPropertySource
    static void billingProperties(DynamicPropertyRegistry registry) {
        registry.add("billing.stripe.secret-key", () -> "sk_test");
        registry.add("billing.stripe.publishable-key", () -> "pk_test");
        registry.add("billing.stripe.webhook-secret", () -> "whsec_test");
        registry.add("billing.stripe.monthly-price-id", () -> "price_monthly");
        registry.add("billing.stripe.portal-configuration-id", () -> "bpc_test");
        registry.add("billing.stripe.default-plan-code", () -> "default-monthly");
    }

    protected void resetBillingScenario() {
        resetScenarioData();
        stubBillingGateway.reset();
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected AuthContext authenticateTenantAdminWithCsrf() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        CsrfContext csrf = CsrfTestClient.fetch(port);
        return new AuthContext(authCookie, csrf.csrfCookie(), csrf.csrfToken());
    }

    protected RequestSpecification givenAuthenticated(AuthContext authContext) {
        return given()
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .contentType(ContentType.JSON);
    }

    protected RequestSpecification givenWebhookRequest() {
        return given()
                .contentType(ContentType.JSON);
    }

    protected BillingSubscriptionEntity seedSubscription(UUID tenantId, String externalCustomerId) {
        BillingSubscriptionEntity entity = new BillingSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(tenantId);
        entity.setProvider(BillingProvider.STRIPE);
        entity.setExternalCustomerId(externalCustomerId);
        entity.setExternalSubscriptionId("sub_seed_123");
        entity.setExternalCheckoutSessionId("cs_seed_123");
        entity.setExternalPriceId("price_monthly");
        entity.setExternalProductId("prod_monthly");
        entity.setPlanCode("default-monthly");
        entity.setStatus(BillingStatus.ACTIVE);
        entity.setInterval(BillingInterval.MONTHLY);
        entity.setCancelAtPeriodEnd(false);
        return billingSubscriptionRepository.save(entity);
    }

    protected record AuthContext(String authCookie, String csrfCookie, String csrfToken) {
    }

}
