package dev.kalles.sale.security.service;

import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.domain.AccountRole;
import dev.kalles.sale.security.domain.PosDeviceSession;
import dev.kalles.sale.security.repository.PosDeviceSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PosDeviceAuthorizationService")
class PosDeviceAuthorizationServiceTest {

    @Mock
    private PosBindingAccessPolicy posBindingAccessPolicy;

    @Mock
    private PosDeviceSessionRepository posDeviceSessionRepository;

    private PosDeviceAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new PosDeviceAuthorizationService(posBindingAccessPolicy, posDeviceSessionRepository);
    }

    @Test
    @DisplayName("nao deve exigir dispositivo pareado para admin")
    void shouldNotRequirePairedDeviceForAdmin() {
        Account account = account(AccountRole.ADMIN, UUID.randomUUID());
        when(posBindingAccessPolicy.requiresPairedDevice(account)).thenReturn(false);

        UUID posId = service.resolveAuthorizedPosId(account, null);

        assertNull(posId);
        verify(posDeviceSessionRepository, never())
                .findByTokenAndActiveTrueAndExpiresAtGreaterThan(any(), any());
    }

    @Test
    @DisplayName("deve bloquear operador sem posToken")
    void shouldBlockOperatorWithoutPosToken() {
        Account account = account(AccountRole.OPERATOR, UUID.randomUUID());
        when(posBindingAccessPolicy.requiresPairedDevice(account)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveAuthorizedPosId(account, null)
        );

        assertEquals("Terminal não configurado. Por favor, solicite o pareamento do caixa.", exception.getMessage());
    }

    @Test
    @DisplayName("deve bloquear operador com token revogado ou invalido")
    void shouldBlockOperatorWithInvalidToken() {
        Account account = account(AccountRole.OPERATOR, UUID.randomUUID());
        when(posBindingAccessPolicy.requiresPairedDevice(account)).thenReturn(true);
        when(posDeviceSessionRepository.findByTokenAndActiveTrueAndExpiresAtGreaterThan(eq("token-revogado"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveAuthorizedPosId(account, "token-revogado")
        );

        assertEquals("Sessão do terminal inválida ou expirada.", exception.getMessage());
    }

    @Test
    @DisplayName("deve bloquear operador com terminal de outra empresa")
    void shouldBlockOperatorWithTokenFromAnotherCompany() {
        UUID accountCompanyId = UUID.randomUUID();
        Account account = account(AccountRole.OPERATOR, accountCompanyId);
        PosDeviceSession session = new PosDeviceSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-outra-empresa",
                LocalDateTime.now().plusDays(30)
        );
        session.setActive(true);

        when(posBindingAccessPolicy.requiresPairedDevice(account)).thenReturn(true);
        when(posDeviceSessionRepository.findByTokenAndActiveTrueAndExpiresAtGreaterThan(eq("token-outra-empresa"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(session));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveAuthorizedPosId(account, "token-outra-empresa")
        );

        assertEquals("Este terminal não pertence a filial do caixa.", exception.getMessage());
    }

    @Test
    @DisplayName("deve retornar posId quando operador usa token valido")
    void shouldReturnPosIdForValidToken() {
        UUID companyId = UUID.randomUUID();
        UUID posId = UUID.randomUUID();
        Account account = account(AccountRole.OPERATOR, companyId);
        PosDeviceSession session = new PosDeviceSession(
                companyId,
                posId,
                "token-valido",
                LocalDateTime.now().plusDays(30)
        );
        session.setActive(true);

        when(posBindingAccessPolicy.requiresPairedDevice(account)).thenReturn(true);
        when(posDeviceSessionRepository.findByTokenAndActiveTrueAndExpiresAtGreaterThan(eq("token-valido"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(session));

        UUID resolvedPosId = service.resolveAuthorizedPosId(account, "token-valido");

        assertEquals(posId, resolvedPosId);
    }

    private Account account(AccountRole role, UUID companyId) {
        Account account = new Account(UUID.randomUUID(), "Conta", "teste@kalles.local", "encoded", role);
        account.setCompanyId(companyId);
        account.setVerified(true);
        return account;
    }
}
