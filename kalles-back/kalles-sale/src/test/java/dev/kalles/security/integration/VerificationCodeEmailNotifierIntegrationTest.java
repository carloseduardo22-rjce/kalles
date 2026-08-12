package dev.kalles.security.integration;

import dev.kalles.email.application.port.in.SendEmailUseCase;
import dev.kalles.email.domain.EmailData;
import dev.kalles.security.event.VerificationCodeIssued;
import dev.kalles.security.support.AbstractSecurityApiContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VerificationCodeEmailNotifierIntegrationTest extends AbstractSecurityApiContainerSupport {

    static class RecordingSendEmailUseCase implements SendEmailUseCase {

        private final List<EmailData> sent = new ArrayList<>();

        @Override
        public void sendEmail(EmailData emailData) {
            sent.add(emailData);
        }
    }

    @TestConfiguration
    static class RecordingEmailConfiguration {

        @Bean
        @Primary
        RecordingSendEmailUseCase recordingSendEmailUseCase() {
            return new RecordingSendEmailUseCase();
        }
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RecordingSendEmailUseCase recordingSendEmailUseCase;

    @BeforeEach
    void setUp() {
        recordingSendEmailUseCase.sent.clear();
    }

    @Test
    void shouldSendVerificationEmailOnlyAfterTransactionCommits() {
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(new VerificationCodeIssued(
                    "dono@kalles.local", "Dono", "123456", 15));

            assertThat(recordingSendEmailUseCase.sent).isEmpty();
        });

        assertThat(recordingSendEmailUseCase.sent).hasSize(1);
        assertThat(recordingSendEmailUseCase.sent.get(0).to()).isEqualTo("dono@kalles.local");
        assertThat(recordingSendEmailUseCase.sent.get(0).variables()).containsEntry("code", "123456");
    }

    @Test
    void shouldNotSendVerificationEmailWhenTransactionRollsBack() {
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(new VerificationCodeIssued(
                    "dono@kalles.local", "Dono", "123456", 15));

            status.setRollbackOnly();
        });

        assertThat(recordingSendEmailUseCase.sent).isEmpty();
    }
}
