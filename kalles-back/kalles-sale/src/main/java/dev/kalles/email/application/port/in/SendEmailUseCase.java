package dev.kalles.email.application.port.in;

import dev.kalles.email.domain.EmailData;

public interface SendEmailUseCase {
    void sendEmail(EmailData emailData);
}