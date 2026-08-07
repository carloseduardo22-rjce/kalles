package dev.kalles.cashregister.validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

    /**
     * Cadeia de validação para abertura de sessão:
     * 1. NoActiveSessionValidator  — caixa já tem sessão ativa?
     * 2. NoActiveOperatorSessionValidator — operador já está em outro caixa?
     */
    @Bean
    public SessionValidator sessionValidatorChain(
            NoActiveSessionValidator noActiveSessionValidator,
            NoActiveOperatorSessionValidator noActiveOperatorSessionValidator
    ) {
        noActiveSessionValidator.setNext(noActiveOperatorSessionValidator);
        return noActiveSessionValidator;
    }
}
