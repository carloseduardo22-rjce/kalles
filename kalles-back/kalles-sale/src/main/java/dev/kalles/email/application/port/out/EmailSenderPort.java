package dev.kalles.email.application.port.out;

import dev.kalles.email.domain.EmailData;

public interface EmailSenderPort {
    void sendHtmlEmail(String to, String subject, String htmlBody);
}