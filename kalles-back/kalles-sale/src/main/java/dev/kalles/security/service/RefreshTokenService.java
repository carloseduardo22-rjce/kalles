package dev.kalles.security.service;

import dev.kalles.security.entity.Account;
import dev.kalles.security.entity.RefreshTokenSession;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.security.repository.RefreshTokenSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final AccountRepository accountRepository;

    @Value("${api.security.refresh-token.expiration-days:30}")
    private Integer refreshTokenExpirationDays;

    @Transactional
    public String issue(Account account) {
        String rawToken = OpaqueTokens.generate();

        RefreshTokenSession session = new RefreshTokenSession();
        session.setAccountId(account.getId());
        session.setTokenHash(OpaqueTokens.sha256Hex(rawToken));
        session.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));

        refreshTokenSessionRepository.save(session);
        return rawToken;
    }

    @Transactional
    public String rotate(String currentRefreshToken) {
        RefreshTokenPrincipal principal = validate(currentRefreshToken);
        revoke(currentRefreshToken);
        return issue(principal.account());
    }

    @Transactional
    public RefreshTokenPrincipal validate(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token ausente ou inválido.");
        }

        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHash(OpaqueTokens.sha256Hex(rawRefreshToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token ausente ou inválido."));

        if (!session.isActiveAt(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expirado ou revogado.");
        }

        Account account = accountRepository.findById(session.getAccountId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Conta da sessão de refresh não encontrada."));

        session.setLastUsedAt(LocalDateTime.now());
        refreshTokenSessionRepository.save(session);

        return new RefreshTokenPrincipal(account);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        refreshTokenSessionRepository.findByTokenHash(OpaqueTokens.sha256Hex(rawRefreshToken))
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
}
