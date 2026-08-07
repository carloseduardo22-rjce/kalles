package dev.kalles.payment.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.security.domain.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class PaymentProviderOAuthStateService {

    @Value("${payment.oauth.state-secret:${api.security.token.secret:my-very-secret-default-key-keep-it-safe}}")
    private String secret;

    @Value("${payment.oauth.state.expiration-minutes:10}")
    private Integer expirationMinutes;

    @Value("${app.security.cookies.secure:false}")
    private boolean secureCookies;

    public String generateState(PaymentProvider provider, Account account) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer("kalles-payment-oauth")
                .withSubject(provider.name())
                .withClaim("provider", provider.name())
                .withClaim("tenantId", account.getTenantId().toString())
                .withClaim("accountId", account.getId().toString())
                .withClaim("nonce", UUID.randomUUID().toString())
                .withExpiresAt(genExpirationDate())
                .sign(algorithm);
    }

    public UUID validateState(PaymentProvider provider, Account account, String providedState, String expectedCookieState) {
        if (providedState == null || providedState.isBlank()) {
            throw new IllegalArgumentException("OAuth state ausente.");
        }
        if (expectedCookieState == null || expectedCookieState.isBlank()) {
            throw new IllegalArgumentException("Estado OAuth da sessao nao encontrado.");
        }
        if (!MessageDigest.isEqual(
                providedState.getBytes(StandardCharsets.UTF_8),
                expectedCookieState.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Estado OAuth invalido para esta sessao.");
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("kalles-payment-oauth")
                    .withSubject(provider.name())
                    .build()
                    .verify(providedState);

            String tokenProvider = jwt.getClaim("provider").asString();
            String tenantId = jwt.getClaim("tenantId").asString();
            String accountId = jwt.getClaim("accountId").asString();

            if (!provider.name().equals(tokenProvider)) {
                throw new IllegalArgumentException("Provider OAuth invalido.");
            }
            if (!account.getTenantId().toString().equals(tenantId) || !account.getId().toString().equals(accountId)) {
                throw new IllegalArgumentException("Estado OAuth nao pertence ao usuario autenticado.");
            }

            return UUID.fromString(tenantId);
        } catch (JWTVerificationException ex) {
            throw new IllegalArgumentException("Estado OAuth expirado ou invalido.");
        }
    }

    public ResponseCookie buildStateCookie(PaymentProvider provider, String state) {
        return ResponseCookie.from(cookieName(provider), state)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(expirationMinutes))
                .build();
    }

    public ResponseCookie buildExpiredStateCookie(PaymentProvider provider) {
        return ResponseCookie.from(cookieName(provider), "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String cookieName(PaymentProvider provider) {
        return "kalles_oauth_state_" + provider.name().toLowerCase();
    }

    private Date genExpirationDate() {
        return Date.from(Instant.now()
                .plusSeconds(expirationMinutes * 60L)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
