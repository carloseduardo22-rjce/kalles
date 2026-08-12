package dev.kalles.security.service;

import dev.kalles.email.application.port.in.SendEmailUseCase;
import dev.kalles.email.domain.EmailData;
import dev.kalles.security.event.VerificationCodeIssued;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeEmailNotifier {

    private final SendEmailUseCase sendEmailUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onVerificationCodeIssued(VerificationCodeIssued event) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", event.name());
        variables.put("code", event.code());
        variables.put("expiresIn", event.expiresInMinutes());

        EmailData emailData = new EmailData(
                event.email(),
                "Código de Verificação - Kalles",
                "email/verification-code",
                variables
        );

        sendEmailUseCase.sendEmail(emailData);
        log.info("Código de verificação enviado para o e-mail: {}", event.email());
    }
}
