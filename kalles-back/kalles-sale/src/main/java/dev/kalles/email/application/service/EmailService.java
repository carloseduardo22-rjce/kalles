package dev.kalles.email.application.service;

import dev.kalles.email.application.port.in.SendEmailUseCase;
import dev.kalles.email.application.port.out.EmailSenderPort;
import dev.kalles.email.application.port.out.TemplateEnginePort;
import dev.kalles.email.domain.EmailData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService implements SendEmailUseCase {

    private final EmailSenderPort emailSenderPort;
    private final TemplateEnginePort templateEnginePort;

    @Override
    public void sendEmail(EmailData emailData) {
        String htmlBody = templateEnginePort.processTemplate(emailData.templateName(), emailData.variables());
        emailSenderPort.sendHtmlEmail(emailData.to(), emailData.subject(), htmlBody);
    }
}