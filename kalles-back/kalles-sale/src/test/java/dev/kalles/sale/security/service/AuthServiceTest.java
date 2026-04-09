package dev.kalles.sale.security.service;

import dev.kalles.sale.core.repository.TenantRepository;
import dev.kalles.sale.security.application.AccountVerificationService;
import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.domain.AccountRole;
import dev.kalles.sale.security.dto.LoginRequest;
import dev.kalles.sale.security.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

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
    private Authentication authentication;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                accountRepository,
                tenantRepository,
                passwordEncoder,
                jwtService,
                accountVerificationService,
                posDeviceAuthorizationService,
                refreshTokenService
        );
    }

    @Test
    @DisplayName("deve autenticar admin sem posToken")
    void shouldAuthenticateAdminWithoutPosToken() {
        Account account = account(AccountRole.ADMIN, UUID.randomUUID());
        LoginRequest request = new LoginRequest(account.getEmail(), "123456");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(account);
        when(posDeviceAuthorizationService.resolveAuthorizedPosId(account, null)).thenReturn(null);
        when(jwtService.generateToken(account, null)).thenReturn("jwt-admin");
        when(refreshTokenService.issue(account, null)).thenReturn("refresh-admin");

        AuthTokens tokens = authService.authenticate(request, null);

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

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(account);
        when(posDeviceAuthorizationService.resolveAuthorizedPosId(account, "token-pos")).thenReturn(posId);
        when(jwtService.generateToken(account, posId)).thenReturn("jwt-operator");
        when(refreshTokenService.issue(account, posId)).thenReturn("refresh-operator");

        AuthTokens tokens = authService.authenticate(request, "token-pos");

        assertEquals("jwt-operator", tokens.accessToken());
        assertEquals("refresh-operator", tokens.refreshToken());
        verify(posDeviceAuthorizationService).resolveAuthorizedPosId(account, "token-pos");
        verify(jwtService).generateToken(account, posId);
        verify(refreshTokenService).issue(account, posId);
    }

    private Account account(AccountRole role, UUID companyId) {
        Account account = new Account(UUID.randomUUID(), "Conta", "conta@kalles.local", "encoded", role);
        account.setCompanyId(companyId);
        account.setVerified(true);
        account.setId(UUID.randomUUID());
        return account;
    }
}
