package dev.kalles.sale.mercadopago.port;

public interface MercadoPagoOAuthPort {
    
    OAuthTokenResponse exchangeCodeForToken(String authorizationCode);
    
    public record OAuthTokenResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        String scope,
        Long userId,
        String refreshToken,
        String publicKey,
        Boolean liveMode
    ) {}
}