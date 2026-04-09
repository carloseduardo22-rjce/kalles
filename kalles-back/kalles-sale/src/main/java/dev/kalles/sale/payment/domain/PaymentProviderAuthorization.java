package dev.kalles.sale.payment.domain;

public record PaymentProviderAuthorization(
        String providerAccountId,
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String scope,
        String publicKey,
        Boolean liveMode
) {
}
