package dev.kalles.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET = "segredo-de-teste-do-kalles";
    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMinutes", 15);
    }

    private Account account() {
        Account account = new Account();
        account.setEmail("operador@kalles.dev");
        account.setTenantId(TENANT_ID);
        account.setRole(AccountRole.ADMIN);
        account.setId(UUID.randomUUID());
        return account;
    }

    @Test
    @DisplayName("deve aceitar o token que ele mesmo emite")
    void shouldAcceptTheTokenItIssues() {
        String token = jwtService.generateToken(account());

        assertThat(jwtService.validateToken(token)).isNotNull();
    }

    @Test
    @DisplayName("deve expirar o token na janela configurada")
    void shouldExpireTheTokenWithinTheConfiguredWindow() {
        String token = jwtService.generateToken(account());

        Instant expiresAt = JWT.decode(token).getExpiresAtAsInstant();

        assertThat(jwtService.accessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(expiresAt).isBefore(Instant.now().plus(Duration.ofMinutes(16)));
    }

    @Test
    @DisplayName("deve recusar token de outro tipo assinado com o mesmo segredo")
    void shouldRejectATokenOfAnotherTypeSignedWithTheSameSecret() {
        String foreignToken = JWT.create()
                .withIssuer("kalles-api")
                .withSubject("operador@kalles.dev")
                .withClaim("tokenType", "oauth-state")
                .withClaim("tenantId", TENANT_ID.toString())
                .withExpiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .sign(Algorithm.HMAC256(SECRET));

        assertThat(jwtService.validateToken(foreignToken)).isNull();
    }

    @Test
    @DisplayName("deve recusar token sem o claim de tipo")
    void shouldRejectATokenWithoutTheTypeClaim() {
        String tokenWithoutType = JWT.create()
                .withIssuer("kalles-api")
                .withSubject("operador@kalles.dev")
                .withClaim("tenantId", TENANT_ID.toString())
                .withExpiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .sign(Algorithm.HMAC256(SECRET));

        assertThat(jwtService.validateToken(tokenWithoutType)).isNull();
    }

    @Test
    @DisplayName("deve recusar token de outro emissor")
    void shouldRejectATokenFromAnotherIssuer() {
        String foreignToken = JWT.create()
                .withIssuer("kalles-payment-oauth")
                .withSubject("operador@kalles.dev")
                .withClaim("tokenType", "access")
                .withExpiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .sign(Algorithm.HMAC256(SECRET));

        assertThat(jwtService.validateToken(foreignToken)).isNull();
    }
}
