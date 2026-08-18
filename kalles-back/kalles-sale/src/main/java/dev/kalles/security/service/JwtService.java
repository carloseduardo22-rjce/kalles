package dev.kalles.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.kalles.security.entity.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.access-token.expiration-minutes:15}")
    private Integer accessTokenExpirationMinutes;

    public Duration accessTokenTtl() {
        return Duration.ofMinutes(accessTokenExpirationMinutes);
    }

    public String generateToken(Account account) {
        return generateToken(account, null);
    }

    public String generateToken(Account account, UUID posId) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var jwtBuilder = JWT.create()
                    .withIssuer("kalles-api")
                    .withSubject(account.getEmail())
                    .withClaim("tokenType", "access")
                    .withClaim("tenantId", account.getTenantId().toString())
                    .withClaim("role", account.getRole().name())
                    .withClaim("accountId", account.getId().toString());

            if (account.getCompanyId() != null) {
                jwtBuilder.withClaim("companyId", account.getCompanyId().toString());
            }
            if (posId != null) {
                jwtBuilder.withClaim("posId", posId.toString());
            }

            return jwtBuilder
                    .withExpiresAt(expiresAt())
                    .sign(algorithm);
        } catch (Exception exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("kalles-api")
                    .withClaim("tokenType", "access")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant expiresAt() {
        return Instant.now().plus(accessTokenTtl());
    }
}
