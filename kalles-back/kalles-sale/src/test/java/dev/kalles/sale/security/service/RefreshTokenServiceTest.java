package dev.kalles.sale.security.service;

import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.domain.AccountRole;
import dev.kalles.sale.security.domain.RefreshTokenSession;
import dev.kalles.sale.security.repository.AccountRepository;
import dev.kalles.sale.security.repository.RefreshTokenSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 30);
    }

    @Test
    @DisplayName("deve emitir refresh token persistido")
    void shouldIssueRefreshToken() {
        Account account = account();

        String rawToken = refreshTokenService.issue(account, UUID.randomUUID());

        assertNotNull(rawToken);
        verify(refreshTokenSessionRepository, atLeastOnce()).save(any(RefreshTokenSession.class));
    }

    @Test
    @DisplayName("deve validar refresh token ativo")
    void shouldValidateActiveRefreshToken() {
        Account account = account();
        String rawToken = refreshTokenService.issue(account, null);

        RefreshTokenSession session = new RefreshTokenSession();
        session.setAccountId(account.getId());
        session.setTokenHash(hash(rawToken));
        session.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenSessionRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(session));
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        RefreshTokenPrincipal principal = refreshTokenService.validate(rawToken);

        assertEquals(account.getId(), principal.account().getId());
        verify(refreshTokenSessionRepository, atLeastOnce()).save(any(RefreshTokenSession.class));
    }

    @Test
    @DisplayName("deve recusar refresh token revogado")
    void shouldRejectRevokedRefreshToken() {
        Account account = account();
        String rawToken = refreshTokenService.issue(account, null);

        RefreshTokenSession session = new RefreshTokenSession();
        session.setAccountId(account.getId());
        session.setTokenHash(hash(rawToken));
        session.setExpiresAt(LocalDateTime.now().plusDays(1));
        session.setRevokedAt(LocalDateTime.now());

        when(refreshTokenSessionRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(session));

        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.validate(rawToken));
    }

    @Test
    @DisplayName("deve rotacionar refresh token")
    void shouldRotateRefreshToken() {
        Account account = account();
        String rawToken = refreshTokenService.issue(account, null);

        RefreshTokenSession session = new RefreshTokenSession();
        session.setAccountId(account.getId());
        session.setTokenHash(hash(rawToken));
        session.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenSessionRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(session));
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        String rotatedToken = refreshTokenService.rotate(rawToken);

        assertNotNull(rotatedToken);
        assertNotEquals(rawToken, rotatedToken);
    }

    @Test
    @DisplayName("deve revogar todas as sessões ativas da conta")
    void shouldRevokeAllActiveSessions() {
        UUID accountId = UUID.randomUUID();
        RefreshTokenSession session = new RefreshTokenSession();
        session.setAccountId(accountId);
        session.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenSessionRepository.findAllByAccountIdAndRevokedAtIsNullAndExpiresAtAfter(any(), any()))
                .thenReturn(List.of(session));

        assertDoesNotThrow(() -> refreshTokenService.revokeAllActiveSessions(accountId));
        verify(refreshTokenSessionRepository).save(session);
    }

    private Account account() {
        Account account = new Account(UUID.randomUUID(), "Conta", "conta@kalles.local", "encoded", AccountRole.ADMIN);
        account.setId(UUID.randomUUID());
        account.setVerified(true);
        return account;
    }

    private String hash(String rawToken) {
        return (String) ReflectionTestUtils.invokeMethod(refreshTokenService, "hash", rawToken);
    }
}
