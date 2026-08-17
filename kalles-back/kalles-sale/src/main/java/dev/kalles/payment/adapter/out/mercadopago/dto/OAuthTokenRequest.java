package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthTokenRequest(
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret,
        String code,
        @JsonProperty("grant_type") String grantType,
        @JsonProperty("redirect_uri") String redirectUri,
        @JsonProperty("test_token") String testToken
) {
}
