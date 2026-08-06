package dev.kalles.sale.client.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kalles.sale.client.entity.Client;
import dev.kalles.sale.client.repository.ClientRepository;
import dev.kalles.sale.security.support.AbstractCompanyContextApiSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractClientApiSupport extends AbstractCompanyContextApiSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Autowired
    protected ClientRepository clientRepository;

    protected void resetClientScenario() {
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

    protected Client seedClient(UUID companyId, String name, String cpf) {
        Client client = new Client();
        client.setCompanyId(companyId);
        client.setName(name);
        client.setCpf(cpf);
        client.setBirthDate(LocalDate.of(1990, 5, 20));
        client.setGender('F');
        client.setCodeCountry("55");
        client.setCellphone("11999999999");
        return clientRepository.save(client);
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
                throw new IllegalStateException("Falha ao obter token CSRF para os testes de client.");
            }

            JsonNode body = OBJECT_MAPPER.readTree(response.body());
            String csrfToken = body.path("token").asText();
            String csrfCookie = response.headers()
                    .allValues("set-cookie")
                    .stream()
                    .map(AbstractClientApiSupport::extractCookieValue)
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElse(null);

            return new CsrfContext(csrfCookie, csrfToken);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a resposta CSRF de client.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requisicao CSRF interrompida durante os testes de client.", e);
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
