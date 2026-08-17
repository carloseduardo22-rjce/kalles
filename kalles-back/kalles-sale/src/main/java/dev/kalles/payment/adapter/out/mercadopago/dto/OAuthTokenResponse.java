package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthTokenResponse(
        @JsonProperty("user_id") String userId,
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn,
        String scope,
        @JsonProperty("public_key") String publicKey,
        @JsonProperty("live_mode") Boolean liveMode
) {
}
