package dev.kalles.fidelity.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kalles.core.entity.FidelityPolicy;
import dev.kalles.core.enums.fidelity.FidelityDiscountType;
import dev.kalles.core.repository.FidelityPolicyRepository;
import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractFidelityPolicyApiSupport extends AbstractCompanyContextApiSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Autowired
    protected FidelityPolicyRepository fidelityPolicyRepository;

    protected void resetFidelityPolicyScenario() {
        fidelityPolicyRepository.deleteAll();
        resetScenarioData();
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected AuthContext authenticateTenantAdminWithCsrf() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        CsrfContext csrf = fetchCsrfToken();
        return new AuthContext(authCookie, csrf.csrfCookie(), csrf.csrfToken());
    }

    protected RequestSpecification givenAuthenticated(AuthContext authContext, UUID companyId) {
        return given()
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .contentType(ContentType.JSON);
    }

    protected FidelityPolicy seedPolicy(
            UUID companyId,
            int objectivePoints,
            String configuredDiscount,
            int valuePoint,
            FidelityDiscountType discountType,
            boolean active,
            LocalDate createdAt
    ) {
        FidelityPolicy policy = new FidelityPolicy();
        policy.setCompanyId(companyId);
        policy.setObjectivePoints(objectivePoints);
        policy.setConfiguredDiscount(new BigDecimal(configuredDiscount));
        policy.setValuePoint(valuePoint);
        policy.setDiscountType(discountType);
        policy.setActive(active);
        policy.setCreatedAt(createdAt);
        return fidelityPolicyRepository.save(policy);
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
                throw new IllegalStateException("Falha ao obter token CSRF para os testes de fidelity policy.");
            }

            JsonNode body = OBJECT_MAPPER.readTree(response.body());
            String csrfToken = body.path("token").asText();
            String csrfCookie = response.headers()
                    .allValues("set-cookie")
                    .stream()
                    .map(AbstractFidelityPolicyApiSupport::extractCookieValue)
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElse(null);

            return new CsrfContext(csrfCookie, csrfToken);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a resposta CSRF de fidelity policy.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requisicao CSRF interrompida durante os testes de fidelity policy.", e);
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
