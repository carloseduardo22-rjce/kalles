
package dev.kalles.security.service;

import dev.kalles.security.entity.Account;
import dev.kalles.security.entity.AccountVerification;
import dev.kalles.security.event.VerificationCodeIssued;
import dev.kalles.security.repository.AccountVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {

    private final AccountVerificationRepository accountVerificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;

    @Transactional
    public void generateAndSendVerificationCode(Account account) {
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        AccountVerification verification = new AccountVerification(account.getId(), code, expiresAt);
        accountVerificationRepository.save(verification);

        eventPublisher.publishEvent(new VerificationCodeIssued(
                account.getEmail(),
                account.getName(),
                code,
                EXPIRATION_MINUTES
        ));
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Transactional
    public void verifyCode(Account account, String code) {
        AccountVerification verification = accountVerificationRepository
                .findFirstByAccountIdAndCodeOrderByCreatedAtDesc(account.getId(), code)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido ou não encontrado."));

        if (verification.isVerified()) {
            throw new IllegalArgumentException("Este código já foi utilizado.");
        }

        if (verification.isExpired()) {
            throw new IllegalArgumentException("Este código expirou. Por favor, solicite um novo.");
        }

        verification.setVerified(true);
        accountVerificationRepository.save(verification);
        
        account.setVerified(true);
        // Note: The caller is responsible for saving the Account entity
    }

    @Transactional
    public void resendCode(Account account) {
        if (account.isVerified()) {
            throw new IllegalArgumentException("A conta já está verificada.");
        }
        generateAndSendVerificationCode(account);
    }
}
