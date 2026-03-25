package dev.kalles.sale.email.application.port.out;

import dev.kalles.sale.email.domain.EmailData;

public interface EmailSenderPort {
    void sendHtmlEmail(String to, String subject, String htmlBody);
}