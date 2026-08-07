package dev.kalles.security.service;

import dev.kalles.security.domain.Account;
import dev.kalles.security.domain.RefreshTokenSession;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.security.repository.RefreshTokenSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final AccountRepository accountRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${api.security.refresh-token.expiration-days:30}")
    private Integer refreshTokenExpirationDays;

    @Transactional
    public String issue(Account account, UUID posId) {
        String rawToken = generateRawToken();

        RefreshTokenSession session = new RefreshTokenSession();
        session.setAccountId(account.getId());
        session.setPosId(posId);
        session.setTokenHash(hash(rawToken));
        session.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));

        refreshTokenSessionRepository.save(session);
        return rawToken;
    }

    @Transactional
    public String rotate(String currentRefreshToken) {
        RefreshTokenPrincipal principal = validate(currentRefreshToken);
        revoke(currentRefreshToken);
        return issue(principal.account(), principal.posId());
    }

    @Transactional
    public RefreshTokenPrincipal validate(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token ausente ou inválido.");
        }

        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token ausente ou inválido."));

        if (!session.isActiveAt(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expirado ou revogado.");
        }

        Account account = accountRepository.findById(session.getAccountId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Conta da sessão de refresh não encontrada."));

        session.setLastUsedAt(LocalDateTime.now());
        refreshTokenSessionRepository.save(session);

        return new RefreshTokenPrincipal(account, session.getPosId());
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        refreshTokenSessionRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(session -> {
                    session.setRevokedAt(LocalDateTime.now());
                    refreshTokenSessionRepository.save(session);
                });
    }

    @Transactional
    public void revokeAllActiveSessions(UUID accountId) {
        for (RefreshTokenSession session :
                refreshTokenSessionRepository.findAllByAccountIdAndRevokedAtIsNullAndExpiresAtAfter(accountId, LocalDateTime.now())) {
            session.setRevokedAt(LocalDateTime.now());
            refreshTokenSessionRepository.save(session);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível no ambiente.", exception);
        }
    }
}
