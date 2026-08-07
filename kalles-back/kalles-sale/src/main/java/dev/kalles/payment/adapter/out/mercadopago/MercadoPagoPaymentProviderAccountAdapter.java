package dev.kalles.payment.adapter.out.mercadopago;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kalles.payment.application.port.out.PaymentProviderAccountPort;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentProviderAuthorization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MercadoPagoPaymentProviderAccountAdapter implements PaymentProviderAccountPort {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final MercadoPagoWebClient mercadoPagoWebClient;

    public MercadoPagoPaymentProviderAccountAdapter(
            @Value("${mercadopago.client-id}") String clientId,
            @Value("${mercadopago.client-secret}") String clientSecret,
            @Value("${mercadopago.redirect-uri}") String redirectUri,
            MercadoPagoWebClient mercadoPagoWebClient
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.mercadoPagoWebClient = mercadoPagoWebClient;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public PaymentProviderAuthorization exchangeAuthorizationCode(String authorizationCode) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("client_id", clientId);
            payload.addProperty("client_secret", clientSecret);
            payload.addProperty("code", authorizationCode);
            payload.addProperty("grant_type", "authorization_code");
            payload.addProperty("redirect_uri", redirectUri);
            payload.addProperty("test_token", "false");

            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/oauth/token",
                    payload.toString(),
                    Map.of("Content-Type", "application/json; charset=UTF-8")
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Failed to exchange OAuth code. Status: " + response.getStatusCode().value());
            }

            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            return new PaymentProviderAuthorization(
                    json.get("user_id").getAsString(),
                    json.get("access_token").getAsString(),
                    json.get("refresh_token").getAsString(),
                    json.get("token_type").getAsString(),
                    json.get("expires_in").getAsLong(),
                    json.has("scope") && !json.get("scope").isJsonNull() ? json.get("scope").getAsString() : null,
                    json.has("public_key") && !json.get("public_key").isJsonNull() ? json.get("public_key").getAsString() : null,
                    json.has("live_mode") && !json.get("live_mode").isJsonNull() ? json.get("live_mode").getAsBoolean() : null
            );
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Error communicating with Mercado Pago OAuth API: " + e.getMessage(), e);
        }
    }
}
