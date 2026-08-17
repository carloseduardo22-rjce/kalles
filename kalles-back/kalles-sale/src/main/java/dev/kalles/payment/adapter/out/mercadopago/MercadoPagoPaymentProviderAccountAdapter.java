package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.adapter.out.mercadopago.dto.OAuthTokenRequest;
import dev.kalles.payment.adapter.out.mercadopago.dto.OAuthTokenResponse;
import dev.kalles.payment.application.port.out.PaymentProviderAccountPort;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentProviderAuthorization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class MercadoPagoPaymentProviderAccountAdapter implements PaymentProviderAccountPort {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final MercadoPagoWebClient mercadoPagoWebClient;
    private final ObjectMapper objectMapper;

    public MercadoPagoPaymentProviderAccountAdapter(
            @Value("${mercadopago.client-id}") String clientId,
            @Value("${mercadopago.client-secret}") String clientSecret,
            @Value("${mercadopago.redirect-uri}") String redirectUri,
            MercadoPagoWebClient mercadoPagoWebClient,
            ObjectMapper objectMapper
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.mercadoPagoWebClient = mercadoPagoWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public PaymentProviderAuthorization exchangeAuthorizationCode(String authorizationCode) {
        try {
            OAuthTokenRequest payload = new OAuthTokenRequest(
                    clientId,
                    clientSecret,
                    authorizationCode,
                    "authorization_code",
                    redirectUri,
                    "false"
            );

            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/oauth/token",
                    objectMapper.writeValueAsString(payload),
                    Map.of("Content-Type", "application/json; charset=UTF-8")
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Failed to exchange OAuth code. Status: " + response.getStatusCode().value());
            }

            OAuthTokenResponse token = objectMapper.readValue(response.getBody(), OAuthTokenResponse.class);
            return new PaymentProviderAuthorization(
                    token.userId(),
                    token.accessToken(),
                    token.refreshToken(),
                    token.tokenType(),
                    token.expiresIn(),
                    token.scope(),
                    token.publicKey(),
                    token.liveMode()
            );
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Error communicating with Mercado Pago OAuth API: " + e.getMessage(), e);
        }
    }
}
