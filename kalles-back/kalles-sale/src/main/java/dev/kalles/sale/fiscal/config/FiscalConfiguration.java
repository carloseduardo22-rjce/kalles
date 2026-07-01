package dev.kalles.sale.fiscal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class FiscalConfiguration {

    @Bean
    Clock fiscalClock() {
        return Clock.systemUTC();
    }
}
