package dev.kalles.security.service;

import dev.kalles.company.repository.TenantRepository;
import dev.kalles.security.application.AccountVerificationService;
import dev.kalles.security.domain.Account;
import dev.kalles.security.domain.AccountRole;
import dev.kalles.security.dto.LoginRequest;
import dev.kalles.security.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AccountVerificationService accountVerificationService;

    @Mock
    private PosDeviceAuthorizationService posDeviceAuthorizationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationProtectionService authenticationProtectionService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                accountRepository,
                tenantRepository,
                passwordEncoder,
                jwtService,
                accountVerificationService,
                posDeviceAuthorizationService,
                refreshTokenService,
                authenticationProtectionService
        );
    }

    @Test
    @DisplayName("deve autenticar admin sem posToken")
    void shouldAuthenticateAdminWithoutPosToken() {
        Account account = account(AccountRole.ADMIN, UUID.randomUUID());
        LoginRequest request = new LoginRequest(account.getEmail(), "123456");

        when(accountRepository.findAllByEmailIgnoreCase(account.getEmail())).thenReturn(List.of(account));
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(posDeviceAuthorizationService.resolveAuthorizedPosId(account, null)).thenReturn(null);
        when(jwtService.generateToken(account, null)).thenReturn("jwt-admin");
        when(refreshTokenService.issue(account, null)).thenReturn("refresh-admin");

        AuthTokens tokens = authService.authenticate(request, null, null);

        assertEquals("jwt-admin", tokens.accessToken());
        assertEquals("refresh-admin", tokens.refreshToken());
        verify(posDeviceAuthorizationService).resolveAuthorizedPosId(account, null);
        verify(jwtService).generateToken(account, null);
        verify(refreshTokenService).issue(account, null);
    }

    @Test
    @DisplayName("deve autenticar operador com posId resolvido do dispositivo")
    void shouldAuthenticateOperatorWithResolvedPosId() {
        UUID posId = UUID.randomUUID();
        Account account = account(AccountRole.OPERATOR, UUID.randomUUID());
        LoginRequest request = new LoginRequest(account.getEmail(), "123456");

        when(accountRepository.findAllByEmailIgnoreCase(account.getEmail())).thenReturn(List.of(account));
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(posDeviceAuthorizationService.resolveAuthorizedPosId(account, "token-pos")).thenReturn(posId);
        when(jwtService.generateToken(account, posId)).thenReturn("jwt-operator");
        when(refreshTokenService.issue(account, posId)).thenReturn("refresh-operator");

        AuthTokens tokens = authService.authenticate(request, "token-pos", null);

        assertEquals("jwt-operator", tokens.accessToken());
        assertEquals("refresh-operator", tokens.refreshToken());
        verify(posDeviceAuthorizationService).resolveAuthorizedPosId(account, "token-pos");
        verify(jwtService).generateToken(account, posId);
        verify(refreshTokenService).issue(account, posId);
    }

    @Test
    @DisplayName("deve exigir tenant quando email existir em mais de um tenant")
    void shouldRequireTenantForDuplicatedEmail() {
        Account tenantA = account(AccountRole.ADMIN, UUID.randomUUID());
        Account tenantB = account(AccountRole.ADMIN, UUID.randomUUID());
        tenantB.setTenantId(UUID.randomUUID());
        LoginRequest request = new LoginRequest(tenantA.getEmail(), "123456");

        when(accountRepository.findAllByEmailIgnoreCase(tenantA.getEmail())).thenReturn(List.of(tenantA, tenantB));

        assertThrows(IllegalArgumentException.class, () -> authService.authenticate(request, null, null));
    }

    @Test
    @DisplayName("deve autenticar email duplicado quando tenant e informado")
    void shouldAuthenticateDuplicatedEmailWithTenant() {
        Account account = account(AccountRole.ADMIN, UUID.randomUUID());
        LoginRequest request = new LoginRequest(account.getEmail(), "123456", account.getTenantId().toString());

        when(accountRepository.findByTenantIdAndEmailIgnoreCase(account.getTenantId(), account.getEmail()))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(posDeviceAuthorizationService.resolveAuthorizedPosId(account, null)).thenReturn(null);
        when(jwtService.generateToken(account, null)).thenReturn("jwt-admin");
        when(refreshTokenService.issue(account, null)).thenReturn("refresh-admin");

        AuthTokens tokens = authService.authenticate(request, null, null);

        assertEquals("jwt-admin", tokens.accessToken());
        assertEquals("refresh-admin", tokens.refreshToken());
    }

    private Account account(AccountRole role, UUID companyId) {
        Account account = new Account(UUID.randomUUID(), "Conta", "conta@kalles.local", "encoded", role);
        account.setCompanyId(companyId);
        account.setVerified(true);
        account.setId(UUID.randomUUID());
        return account;
    }
}
