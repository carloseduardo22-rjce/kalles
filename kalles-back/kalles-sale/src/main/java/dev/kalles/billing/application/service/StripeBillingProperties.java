package dev.kalles.billing.application.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "billing.stripe")
public record StripeBillingProperties(
        String secretKey,
        String publishableKey,
        String webhookSecret,
        String monthlyPriceId,
        String portalConfigurationId,
        String defaultPlanCode
) {
}
