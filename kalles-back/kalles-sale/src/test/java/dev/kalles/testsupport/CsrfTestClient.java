package dev.kalles.testsupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Busca o par cookie/token CSRF que os endpoints protegidos exigem. As classes de suporte
 * mantem cada uma a sua copia dessa logica; novos testes devem usar este helper.
 */
public final class CsrfTestClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String COOKIE_PREFIX = "XSRF-TOKEN=";

    private CsrfTestClient() {
    }

    public record CsrfContext(String csrfCookie, String csrfToken) {
    }

    public static CsrfContext fetch(int port) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/csrf"))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("Falha ao obter token CSRF para os testes.");
            }

            JsonNode body = OBJECT_MAPPER.readTree(response.body());
            String csrfCookie = response.headers()
                    .allValues("set-cookie")
                    .stream()
                    .map(CsrfTestClient::extractCookieValue)
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Resposta CSRF nao trouxe o cookie XSRF-TOKEN."));

            return new CsrfContext(csrfCookie, body.path("token").asText());
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a resposta CSRF de teste.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido ao obter token CSRF de teste.", e);
        }
    }

    private static Optional<String> extractCookieValue(String headerValue) {
        if (headerValue == null || !headerValue.startsWith(COOKIE_PREFIX)) {
            return Optional.empty();
        }
        String cookie = headerValue.split(";", 2)[0];
        return Optional.of(cookie.substring(COOKIE_PREFIX.length()));
    }
}
