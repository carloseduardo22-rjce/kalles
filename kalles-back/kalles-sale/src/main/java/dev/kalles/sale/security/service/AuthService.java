package dev.kalles.sale.security.service;

import dev.kalles.sale.core.entity.Tenant;
import dev.kalles.sale.core.repository.TenantRepository;
import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.domain.AccountRole;
import dev.kalles.sale.security.repository.AccountRepository;
import dev.kalles.sale.security.dto.LoginRequest;
import dev.kalles.sale.security.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import dev.kalles.sale.security.repository.PosDeviceSessionRepository;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    // We ideally use the core generic Tenant, but reusing the one from MP module
    // since it's the root owner
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final dev.kalles.sale.security.application.AccountVerificationService accountVerificationService;
    private final PosDeviceSessionRepository posDeviceSessionRepository;

    public String authenticate(LoginRequest request, String posToken) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var account = (Account) auth.getPrincipal();

        UUID posId = null;

        if (account.getRole() == AccountRole.OPERATOR || account.getCompanyId() != null) {
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
        }

        return jwtService.generateToken(account, posId);
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (accountRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

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
        return "Conta criada com sucesso. Verifique seu e-mail.";
    }

    @Transactional
    public String verifyCode(dev.kalles.sale.security.dto.VerifyCodeRequest request) {
        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        if (account.isVerified()) {
            throw new IllegalArgumentException("Conta já está verificada.");
        }

        accountVerificationService.verifyCode(account, request.code());
        // Since we opened a transaction, and Hibernate manages `account`, it could
        // auto-save.
        // But let's be explicit:
        accountRepository.save(account);

        return jwtService.generateToken(account);
    }

    @Transactional
    public void resendVerificationCode(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        accountVerificationService.resendCode(account);
    }
}