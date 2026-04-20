package dev.kalles.sale.billing.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kalles.sale.billing.adapter.out.persistence.entity.BillingSubscriptionEntity;
import dev.kalles.sale.billing.adapter.out.persistence.repository.SpringDataBillingWebhookEventRepository;
import dev.kalles.sale.billing.adapter.out.persistence.repository.SpringDataBillingSubscriptionRepository;
import dev.kalles.sale.billing.domain.BillingInterval;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import dev.kalles.sale.security.support.AbstractCompanyContextApiSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = dev.kalles.sale.KallesSaleApplication.class)
@Import(BillingTestConfiguration.class)
public abstract class AbstractBillingApiSupport extends AbstractCompanyContextApiSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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
        billingWebhookEventRepository.deleteAll();
        billingSubscriptionRepository.deleteAll();
        stubBillingGateway.reset();
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected AuthContext authenticateTenantAdminWithCsrf() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        CsrfContext csrf = fetchCsrfToken();
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

    protected record CsrfContext(String csrfCookie, String csrfToken) {
    }

    private CsrfContext fetchCsrfToken() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/csrf"))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Falha ao obter token CSRF para os testes de billing.");
            }

            JsonNode body = OBJECT_MAPPER.readTree(response.body());
            String csrfToken = body.path("token").asText();
            String csrfCookie = response.headers()
                    .allValues("set-cookie")
                    .stream()
                    .map(AbstractBillingApiSupport::extractCookieValue)
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElse(null);

            return new CsrfContext(csrfCookie, csrfToken);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a resposta CSRF de billing.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requisicao CSRF interrompida durante os testes de billing.", e);
        }
    }

    private static Optional<String> extractCookieValue(String headerValue) {
        if (headerValue == null || !headerValue.startsWith("XSRF-TOKEN=")) {
            return Optional.empty();
        }

        int separator = headerValue.indexOf(';');
        String cookie = separator >= 0 ? headerValue.substring(0, separator) : headerValue;
        return Optional.of(cookie.substring("XSRF-TOKEN=".length()));
    }
}
