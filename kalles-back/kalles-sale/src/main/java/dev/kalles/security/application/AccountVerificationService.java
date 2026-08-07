
package dev.kalles.security.application;

import dev.kalles.email.domain.EmailData;
import dev.kalles.email.application.port.in.SendEmailUseCase;
import dev.kalles.security.domain.Account;
import dev.kalles.security.domain.AccountVerification;
import dev.kalles.security.domain.AccountVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountVerificationService {

    private final AccountVerificationRepository accountVerificationRepository;
    private final SendEmailUseCase sendEmailUseCase;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;

    @Transactional
    public void generateAndSendVerificationCode(Account account) {
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        AccountVerification verification = new AccountVerification(account.getId(), code, expiresAt);
        accountVerificationRepository.save(verification);

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", account.getName());
        variables.put("code", code);
        variables.put("expiresIn", EXPIRATION_MINUTES);

        EmailData emailData = new EmailData(
                account.getEmail(),
                "Código de Verificação - Kalles",
                "email/verification-code",
                variables
        );

        sendEmailUseCase.sendEmail(emailData);
        log.info("Código de verificação enviado para o e-mail: {}", account.getEmail());
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
