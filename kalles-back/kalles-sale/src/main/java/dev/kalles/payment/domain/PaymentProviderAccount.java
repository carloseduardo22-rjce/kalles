package dev.kalles.payment.domain;

import java.util.Objects;
import java.util.UUID;

public record PaymentProviderAccount(
        UUID tenantId,
        PaymentProvider provider,
        String providerAccountId,
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String scope,
        String publicKey,
        Boolean liveMode
) {

    public PaymentProviderAccount {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(provider, "provider is required");
    }

    public boolean isLinked() {
        return accessToken != null && !accessToken.isBlank()
                && providerAccountId != null && !providerAccountId.isBlank();
    }

    public PaymentProviderAccount withAuthorization(
            String newProviderAccountId,
            String newAccessToken,
            String newRefreshToken,
            String newTokenType,
            Long newExpiresIn,
            String newScope,
            String newPublicKey,
            Boolean newLiveMode
    ) {
        return new PaymentProviderAccount(
                tenantId,
                provider,
                newProviderAccountId,
                newAccessToken,
                newRefreshToken,
                newTokenType,
                newExpiresIn,
                newScope,
                newPublicKey,
                newLiveMode
        );
    }

    public PaymentProviderAccount withAuthorization(PaymentProviderAuthorization authorization) {
        return withAuthorization(
                authorization.providerAccountId(),
                authorization.accessToken(),
                authorization.refreshToken(),
                authorization.tokenType(),
                authorization.expiresIn(),
                authorization.scope(),
                authorization.publicKey(),
                authorization.liveMode()
        );
    }
}
