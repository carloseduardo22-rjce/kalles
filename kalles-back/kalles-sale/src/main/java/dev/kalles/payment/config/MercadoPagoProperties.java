package dev.kalles.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(
        String appId,
        String accessToken,
        String userId,
        String clientId,
        String clientSecret,
        String redirectUri,
        String webhookSecret
) {
}
