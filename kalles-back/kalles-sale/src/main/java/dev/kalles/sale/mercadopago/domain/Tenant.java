package dev.kalles.sale.mercadopago.domain;

import java.util.UUID;

public record Tenant(
        UUID id,
        String name,
        String mpAccessToken,
        String mpRefreshToken,
        String mpUserId) {
            
    public boolean hasMercadoPagoLinked() {
        return mpAccessToken != null && mpUserId != null;
    }

    public Tenant withOAuthCredentials(String accessToken, String refreshToken, String userId) {
        return new Tenant(id, name, accessToken, refreshToken, userId);
    }
}
