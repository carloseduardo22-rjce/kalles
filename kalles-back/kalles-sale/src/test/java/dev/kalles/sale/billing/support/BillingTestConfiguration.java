package dev.kalles.sale.billing.support;

import dev.kalles.sale.billing.application.port.out.BillingGateway;
import dev.kalles.sale.billing.domain.BillingInterval;
import dev.kalles.sale.billing.domain.BillingProvider;
import dev.kalles.sale.billing.domain.BillingStatus;
import dev.kalles.sale.billing.exception.BillingIntegrationException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.util.UUID;

@TestConfiguration
public class BillingTestConfiguration {

    @Bean
    @Primary
    public StubBillingGateway stubBillingGateway() {
        return new StubBillingGateway();
    }

    public static class StubBillingGateway implements BillingGateway {

        private CheckoutCommand lastCheckoutCommand;
        private PortalCommand lastPortalCommand;
        private WebhookNotification nextWebhookNotification;
        private RuntimeException nextWebhookException;

        @Override
        public CheckoutSession createSubscriptionCheckout(CheckoutCommand command) {
            lastCheckoutCommand = command;
            return new CheckoutSession(
                    "cs_test_123",
                    "secret_test_123",
                    "https://checkout.stripe.test/cs_test_123",
                    command.customerId() == null ? "cus_test_123" : command.customerId(),
                    "sub_test_123",
                    BillingStatus.CHECKOUT_CREATED
            );
        }

        @Override
        public PortalSession createPortalSession(PortalCommand command) {
            lastPortalCommand = command;
            return new PortalSession("https://billing.stripe.test/portal-session");
        }

        @Override
        public WebhookNotification parseWebhook(String payload, String signature) {
            if (nextWebhookException != null) {
                RuntimeException exception = nextWebhookException;
                nextWebhookException = null;
                throw exception;
            }

            if (nextWebhookNotification != null) {
                WebhookNotification notification = nextWebhookNotification;
                nextWebhookNotification = null;
                return notification;
            }

            return new WebhookNotification(
                    "evt_test_123",
                    "customer.subscription.updated",
                    BillingProvider.STRIPE,
                    UUID.randomUUID(),
                    "cus_test_123",
                    "sub_test_123",
                    null,
                    "prod_monthly",
                    "price_monthly",
                    BillingStatus.ACTIVE,
                    BillingInterval.MONTHLY,
                    Instant.parse("2026-04-01T00:00:00Z"),
                    Instant.parse("2026-05-01T00:00:00Z"),
                    false
            );
        }

        public CheckoutCommand lastCheckoutCommand() {
            return lastCheckoutCommand;
        }

        public PortalCommand lastPortalCommand() {
            return lastPortalCommand;
        }

        public void setNextWebhookNotification(WebhookNotification nextWebhookNotification) {
            this.nextWebhookNotification = nextWebhookNotification;
        }

        public void failNextWebhook(String message) {
            this.nextWebhookException = new BillingIntegrationException(message);
        }

        public void reset() {
            lastCheckoutCommand = null;
            lastPortalCommand = null;
            nextWebhookNotification = null;
            nextWebhookException = null;
        }
    }
}
