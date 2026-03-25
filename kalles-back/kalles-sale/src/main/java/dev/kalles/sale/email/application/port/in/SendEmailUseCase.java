package dev.kalles.sale.email.application.port.in;

import dev.kalles.sale.email.domain.EmailData;

public interface SendEmailUseCase {
    void sendEmail(EmailData emailData);
}