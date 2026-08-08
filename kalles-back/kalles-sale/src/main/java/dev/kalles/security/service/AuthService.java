package dev.kalles.security.service;

import dev.kalles.company.entity.Tenant;
import dev.kalles.company.repository.TenantRepository;
import dev.kalles.security.dto.LoginRequest;
import dev.kalles.security.dto.RegisterRequest;
import dev.kalles.security.dto.RegisterResponse;
import dev.kalles.security.dto.VerifyCodeRequest;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccountVerificationService accountVerificationService;
    private final PosDeviceAuthorizationService posDeviceAuthorizationService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationProtectionService authenticationProtectionService;

    public AuthTokens authenticate(LoginRequest request, String posToken, String clientFingerprint) {
        authenticationProtectionService.assertLoginAllowed(request.email(), request.tenantId(), clientFingerprint);
        try {
            var account = resolveAccount(request.email(), request.tenantId());
            if (!passwordEncoder.matches(request.password(), account.getPassword())) {
                throw new IllegalArgumentException("Credenciais invalidas.");
            }
            if (!account.isVerified()) {
                throw new IllegalArgumentException("Conta ainda nao verificada.");
            }

            UUID posId = posDeviceAuthorizationService.resolveAuthorizedPosId(account, posToken);

        /* if (account.getRole() == AccountRole.OPERATOR || account.getCompanyId() != null) {
            if (posToken == null || posToken.isBlank()) {
                throw new IllegalArgumentException(
                        "Terminal não configurado. Por favor, solicite o pareamento do caixa.");
            }

            var session = posDeviceSessionRepository
                    .findByTokenAndActiveTrueAndExpiresAtGreaterThan(posToken, LocalDateTime.now())
                    .orElseThrow(() -> new IllegalArgumentException("Sessão do terminal inválida ou expirada."));

            if (!session.getCompanyId().equals(account.getCompanyId())) {
                throw new IllegalArgumentException("Este terminal não pertence a filial do caixa.");
            }

            posId = session.getPosId();
        } */

            authenticationProtectionService.registerLoginSuccess(request.email(), request.tenantId(), clientFingerprint);
            return buildSessionTokens(account, posId);
        } catch (RuntimeException ex) {
            authenticationProtectionService.registerLoginFailure(request.email(), request.tenantId(), clientFingerprint);
            throw ex;
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Create Tenant first
        UUID tenantId = UUID.randomUUID();
        Tenant newTenant = new Tenant(
                tenantId, request.companyName());
        tenantRepository.save(newTenant);

        // Create Account
        String encryptedPassword = passwordEncoder.encode(request.password());
        Account newAccount = new Account(
                tenantId,
                request.name(),
                request.email(),
                encryptedPassword,
                AccountRole.ADMIN);

        accountRepository.save(newAccount);

        // Generate and send verification code
        accountVerificationService.generateAndSendVerificationCode(newAccount);

        // Do not generate token immediately because the account is not verified yet.
        // Return something empty or a success message.
        return new RegisterResponse(tenantId.toString(), "Conta criada com sucesso. Verifique seu e-mail.");
    }

    @Transactional
    public AuthTokens verifyCode(VerifyCodeRequest request, String clientFingerprint) {
        authenticationProtectionService.assertVerificationAllowed(request.email(), request.tenantId(), clientFingerprint);
        try {
            Account account = resolveAccountOptional(request.email(), request.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

            if (account.isVerified()) {
            throw new IllegalArgumentException("Conta já está verificada.");
        }

            accountVerificationService.verifyCode(account, request.code());
        // Since we opened a transaction, and Hibernate manages `account`, it could
        // auto-save.
        // But let's be explicit:
            accountRepository.save(account);
            authenticationProtectionService.registerVerificationSuccess(request.email(), request.tenantId(), clientFingerprint);

            return buildSessionTokens(account, null);
        } catch (RuntimeException ex) {
            authenticationProtectionService.registerVerificationFailure(request.email(), request.tenantId(), clientFingerprint);
            throw ex;
            }
    }

    @Transactional
    public void resendVerificationCode(String email, String tenantId, String clientFingerprint) {
        authenticationProtectionService.assertResendAllowed(email, tenantId, clientFingerprint);
        Account account = resolveAccountOptional(email, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        accountVerificationService.resendCode(account);
    }
    @Transactional
    public AuthTokens refresh(String rawRefreshToken) {
        RefreshTokenPrincipal principal = refreshTokenService.validate(rawRefreshToken);
        String accessToken = jwtService.generateToken(principal.account(), principal.posId());
        String nextRefreshToken = refreshTokenService.rotate(rawRefreshToken);
        return new AuthTokens(accessToken, nextRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthTokens buildSessionTokens(Account account, UUID posId) {
        String accessToken = jwtService.generateToken(account, posId);
        String refreshToken = refreshTokenService.issue(account, posId);
        return new AuthTokens(accessToken, refreshToken);
    }

    private Account resolveAccount(String email, String tenantId) {
        return resolveAccountOptional(email, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Conta nao encontrada."));
    }

    private Optional<Account> resolveAccountOptional(String email, String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            UUID parsedTenantId = UUID.fromString(tenantId);
            return accountRepository.findByTenantIdAndEmailIgnoreCase(parsedTenantId, email);
        }

        var matches = accountRepository.findAllByEmailIgnoreCase(email);
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Tenant obrigatorio para este e-mail.");
        }
        return matches.stream().findFirst();
    }
}
