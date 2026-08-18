package dev.kalles.billing.adapter.out.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.billingportal.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionRetrieveParams;
import dev.kalles.billing.domain.BillingInterval;
import dev.kalles.billing.domain.BillingStatus;
import dev.kalles.billing.exception.BillingIntegrationException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class DefaultStripeSdkClient implements StripeSdkClient {

    public DefaultStripeSdkClient(String secretKey) {
        Stripe.apiKey = secretKey;
    }

    @Override
    public StripeCustomer createCustomer(String email, String name, Map<String, String> metadata) {
        try {
            CustomerCreateParams.Builder builder = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name);

            metadata.forEach(builder::putMetadata);

            var customer = com.stripe.model.Customer.create(builder.build());
            return new StripeCustomer(customer.getId(), customer.getEmail(), customer.getName());
        } catch (StripeException e) {
            log.error("Falha ao criar cliente na Stripe", e);
            throw new BillingIntegrationException("Falha ao criar cliente na Stripe.", e);
        }
    }

    @Override
    public StripeCheckoutSession createSubscriptionCheckout(StripeCheckoutRequest request) {
        try {
            com.stripe.param.checkout.SessionCreateParams params = com.stripe.param.checkout.SessionCreateParams.builder()
                    .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(request.customerId())
                    .setReturnUrl(request.returnUrl())
                    .setUiMode(com.stripe.param.checkout.SessionCreateParams.UiMode.EMBEDDED)
                    .putMetadata("tenantId", request.tenantId().toString())
                    .setClientReferenceId(request.tenantId().toString())
                    .setSubscriptionData(
                            com.stripe.param.checkout.SessionCreateParams.SubscriptionData.builder()
                                    .putMetadata("tenantId", request.tenantId().toString())
                                    .build()
                    )
                    .addLineItem(
                            com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                                    .setPrice(request.priceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            var session = com.stripe.model.checkout.Session.create(params);
            String subscriptionId = session.getSubscription() instanceof String value ? value : null;

            return new StripeCheckoutSession(
                    session.getId(),
                    session.getClientSecret(),
                    session.getUrl(),
                    session.getCustomer(),
                    subscriptionId,
                    BillingStatus.CHECKOUT_CREATED
            );
        } catch (StripeException e) {
            log.error("Falha ao criar checkout de assinatura na Stripe para o tenant {}", request.tenantId(), e);
            throw new BillingIntegrationException("Falha ao criar checkout de assinatura na Stripe.", e);
        }
    }

    @Override
    public StripePortalSession createPortalSession(String customerId, String returnUrl, String configurationId) {
        try {
            com.stripe.param.billingportal.SessionCreateParams.Builder builder = com.stripe.param.billingportal.SessionCreateParams.builder()
                    .setCustomer(customerId)
                    .setReturnUrl(returnUrl);

            if (configurationId != null && !configurationId.isBlank()) {
                builder.setConfiguration(configurationId);
            }

            Session session = Session.create(builder.build());
            return new StripePortalSession(session.getUrl());
        } catch (StripeException e) {
            log.error("Falha ao criar sessao do portal do cliente na Stripe para o cliente {}", customerId, e);
            throw new BillingIntegrationException("Falha ao criar sessao do portal do cliente na Stripe.", e);
        }
    }

    @Override
    public StripeWebhookPayload parseWebhook(String payload, String signature, String webhookSecret) {
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            return switch (event.getType()) {
                case "checkout.session.completed" -> mapCheckoutCompleted(event);
                case "customer.subscription.created", "customer.subscription.updated", "customer.subscription.deleted" ->
                        mapSubscriptionEvent(event);
                case "invoice.payment_failed" -> mapInvoicePaymentFailed(event);
                default -> new StripeWebhookPayload(
                        event.getId(),
                        event.getType(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false
                );
            };
        } catch (Exception e) {
            log.warn("Webhook Stripe rejeitado na validacao: assinatura={}", signature != null && !signature.isBlank(), e);
            throw new BillingIntegrationException("Falha ao validar webhook Stripe.", e);
        }
    }

    @Override
    public StripeSubscription retrieveSubscription(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(
                    subscriptionId,
                    SubscriptionRetrieveParams.builder().addExpand("items.data.price.product").build(),
                    null
            );
            return mapSubscription(subscription);
        } catch (StripeException e) {
            log.error("Falha ao consultar a assinatura {} na Stripe", subscriptionId, e);
            throw new BillingIntegrationException("Falha ao consultar assinatura na Stripe.", e);
        }
    }

    private StripeWebhookPayload mapCheckoutCompleted(Event event) {
        var session = (com.stripe.model.checkout.Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new BillingIntegrationException("Nao foi possivel desserializar checkout.session.completed."));

        String subscriptionId = session.getSubscription() instanceof String value ? value : null;
        StripeSubscription subscription = subscriptionId == null ? null : retrieveSubscription(subscriptionId);

        return new StripeWebhookPayload(
                event.getId(),
                event.getType(),
                resolveTenantId(subscription != null ? subscription.metadata() : session.getMetadata(), session.getClientReferenceId()),
                session.getCustomer(),
                subscriptionId,
                session.getId(),
                subscription != null ? subscription.priceId() : null,
                subscription != null ? subscription.productId() : null,
                subscription != null ? subscription.status() : BillingStatus.CHECKOUT_CREATED,
                subscription != null ? subscription.interval() : BillingInterval.MONTHLY,
                subscription != null ? subscription.currentPeriodStart() : null,
                subscription != null ? subscription.currentPeriodEnd() : null,
                subscription != null && subscription.cancelAtPeriodEnd()
        );
    }

    private StripeWebhookPayload mapSubscriptionEvent(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new BillingIntegrationException("Nao foi possivel desserializar customer.subscription event."));

        StripeSubscription mappedSubscription = mapSubscription(subscription);

        return new StripeWebhookPayload(
                event.getId(),
                event.getType(),
                resolveTenantId(mappedSubscription.metadata(), null),
                mappedSubscription.customerId(),
                mappedSubscription.id(),
                null,
                mappedSubscription.priceId(),
                mappedSubscription.productId(),
                mappedSubscription.status(),
                mappedSubscription.interval(),
                mappedSubscription.currentPeriodStart(),
                mappedSubscription.currentPeriodEnd(),
                mappedSubscription.cancelAtPeriodEnd()
        );
    }

    private StripeWebhookPayload mapInvoicePaymentFailed(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new BillingIntegrationException("Nao foi possivel desserializar invoice.payment_failed."));

        String subscriptionId = invoice.getParent() == null || invoice.getParent().getSubscriptionDetails() == null
                ? null
                : invoice.getParent().getSubscriptionDetails().getSubscription();
        StripeSubscription subscription = subscriptionId == null
                ? null
                : retrieveSubscription(subscriptionId);

        return new StripeWebhookPayload(
                event.getId(),
                event.getType(),
                subscription == null ? null : resolveTenantId(subscription.metadata(), null),
                invoice.getCustomer(),
                subscription == null ? null : subscription.id(),
                null,
                subscription == null ? null : subscription.priceId(),
                subscription == null ? null : subscription.productId(),
                BillingStatus.PAST_DUE,
                subscription == null ? null : subscription.interval(),
                subscription == null ? null : subscription.currentPeriodStart(),
                subscription == null ? null : subscription.currentPeriodEnd(),
                subscription != null && subscription.cancelAtPeriodEnd()
        );
    }

    private StripeSubscription mapSubscription(Subscription subscription) {
        var item = subscription.getItems().getData().get(0);
        Price price = item.getPrice();
        String productId = price.getProduct() instanceof String value ? value : null;

        return new StripeSubscription(
                subscription.getId(),
                subscription.getCustomer(),
                price.getId(),
                productId,
                mapStatus(subscription.getStatus()),
                BillingInterval.MONTHLY,
                toInstant(item.getCurrentPeriodStart()),
                toInstant(item.getCurrentPeriodEnd()),
                Boolean.TRUE.equals(subscription.getCancelAtPeriodEnd()),
                subscription.getMetadata()
        );
    }

    private BillingStatus mapStatus(String status) {
        if (status == null) {
            return BillingStatus.INCOMPLETE;
        }

        return switch (status) {
            case "trialing" -> BillingStatus.TRIALING;
            case "active" -> BillingStatus.ACTIVE;
            case "past_due" -> BillingStatus.PAST_DUE;
            case "canceled" -> BillingStatus.CANCELED;
            case "unpaid" -> BillingStatus.UNPAID;
            case "incomplete_expired" -> BillingStatus.INCOMPLETE_EXPIRED;
            default -> BillingStatus.INCOMPLETE;
        };
    }

    private Instant toInstant(Long epochSeconds) {
        if (epochSeconds == null) {
            return null;
        }
        return Instant.ofEpochSecond(epochSeconds);
    }

    private UUID resolveTenantId(Map<String, String> metadata, String clientReferenceId) {
        String tenantId = Optional.ofNullable(metadata)
                .map(values -> values.get("tenantId"))
                .orElse(clientReferenceId);

        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }

        return UUID.fromString(tenantId);
    }
}
