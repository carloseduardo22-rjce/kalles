package dev.kalles.sale.email.application.port.out;

import java.util.Map;

public interface TemplateEnginePort {
    String processTemplate(String templateName, Map<String, Object> variables);
}