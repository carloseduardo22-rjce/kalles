package dev.kalles.sale.security.service;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.TenantEntity;
import dev.kalles.sale.mercadopago.port.TenantRepository;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    // We ideally use the core generic Tenant, but reusing the one from MP module since it's the root owner
    private final TenantRepository tenantRepository; 
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final dev.kalles.sale.security.application.AccountVerificationService accountVerificationService;

    public String authenticate(LoginRequest request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var account = (Account) auth.getPrincipal();
        return jwtService.generateToken(account);
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (accountRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        // Create Tenant first
        UUID tenantId = UUID.randomUUID();
        dev.kalles.sale.mercadopago.domain.Tenant newTenant = new dev.kalles.sale.mercadopago.domain.Tenant(
                tenantId, request.companyName(), null, null, null
        );
        tenantRepository.save(newTenant);

        // Create Account
        String encryptedPassword = passwordEncoder.encode(request.password());
        Account newAccount = new Account(
                tenantId,
                request.name(),
                request.email(),
                encryptedPassword,
                AccountRole.ADMIN
        );
        
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
        // Since we opened a transaction, and Hibernate manages `account`, it could auto-save.
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