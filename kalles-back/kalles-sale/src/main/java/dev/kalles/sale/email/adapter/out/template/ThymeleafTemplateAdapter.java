package dev.kalles.sale.email.adapter.out.template;

import dev.kalles.sale.email.application.port.out.TemplateEnginePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ThymeleafTemplateAdapter implements TemplateEnginePort {

    private final TemplateEngine templateEngine;

    @Override
    public String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}