package dev.kalles.email.adapter.out.smtp;

import dev.kalles.email.application.port.out.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender javaMailSender;
    private final String username;
    private final String password;

    public SmtpEmailSenderAdapter(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password
    ) {
        this.javaMailSender = javaMailSender;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean isConfigured() {
        return !username.isBlank() && !password.isBlank();
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates html
            
            javaMailSender.send(message);
            log.debug("E-mail entregue ao servidor SMTP para {} com assunto \"{}\"", to, subject);
        } catch (MessagingException e) {
            log.error("Falha ao montar e-mail para {} com assunto \"{}\"", to, subject, e);
            throw new RuntimeException("Failed to send email to: " + to, e);
        } catch (MailException e) {
            log.error("Falha ao entregar e-mail para {} com assunto \"{}\"", to, subject, e);
            throw e;
        }
    }
}