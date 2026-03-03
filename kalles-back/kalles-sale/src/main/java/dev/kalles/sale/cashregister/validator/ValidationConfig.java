package dev.kalles.sale.cashregister.validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

    @Bean
    public SessionValidator sessionValidatorChain(
            NoActiveSessionValidator noActiveSessionValidator
    ) {
        return noActiveSessionValidator;
    }
}
