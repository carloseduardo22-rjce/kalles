package dev.kalles.sale.email.domain;

import java.util.Map;

public record EmailData(
        String to,
        String subject,
        String templateName,
        Map<String, Object> variables
) {
}