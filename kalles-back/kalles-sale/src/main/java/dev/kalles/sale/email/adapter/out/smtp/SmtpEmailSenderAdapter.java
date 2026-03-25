package dev.kalles.sale.email.adapter.out.smtp;

import dev.kalles.sale.email.application.port.out.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates html
            
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to: " + to, e);
        }
    }
}