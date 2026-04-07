package dev.kalles.sale.billing.config;

import dev.kalles.sale.billing.adapter.out.stripe.DefaultStripeSdkClient;
import dev.kalles.sale.billing.adapter.out.stripe.StripeSdkClient;
import dev.kalles.sale.billing.application.service.StripeBillingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeBillingProperties.class)
public class StripeBillingConfiguration {

    @Bean
    public StripeSdkClient stripeSdkClient(StripeBillingProperties stripeBillingProperties) {
        return new DefaultStripeSdkClient(stripeBillingProperties.secretKey());
    }
}
