package dev.kalles.fiscal.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FiscalTestConfiguration {

    @Bean
    @Primary
    ControllableSefazAuthorizationPort controllableSefazAuthorizationPort() {
        return new ControllableSefazAuthorizationPort();
    }
}
