package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.MercadoPagoOAuthPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class MercadoPagoOAuthAdapter implements MercadoPagoOAuthPort {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final HttpClient jdkClient;

    public MercadoPagoOAuthAdapter(
            @Value("${mercadopago.client-id}") String clientId,
            @Value("${mercadopago.client-secret}") String clientSecret,
            @Value("${mercadopago.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.jdkClient = HttpClient.newHttpClient();
    }

    @Override
    public OAuthTokenResponse exchangeCodeForToken(String authorizationCode) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("client_id", clientId);
            payload.addProperty("client_secret", clientSecret);
            payload.addProperty("code", authorizationCode);
            payload.addProperty("grant_type", "authorization_code");
            payload.addProperty("redirect_uri", redirectUri);
            payload.addProperty("test_token", "false"); // Pode ser dinâmico depois

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mercadopago.com/oauth/token"))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = jdkClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                return new OAuthTokenResponse(
                        json.get("access_token").getAsString(),
                        json.get("token_type").getAsString(),
                        json.get("expires_in").getAsLong(),
                        json.has("scope") ? json.get("scope").getAsString() : null,
                        json.get("user_id").getAsLong(),
                        json.get("refresh_token").getAsString(),
                        json.get("public_key").getAsString(),
                        json.has("live_mode") && json.get("live_mode").getAsBoolean()
                );
            } else {
                System.err.println(">>> [MercadoPagoOAuthAdapter] Error exchanging code: " + response.body());
                throw new MercadoPagoIntegrationException("Failed to exchange OAuth code. Status: " + response.statusCode());
            }

        } catch (Exception e) {
            throw new MercadoPagoIntegrationException("Error communicating with Mercado Pago OAuth API: " + e.getMessage(), e);
        }
    }
}
