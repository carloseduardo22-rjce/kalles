package dev.kalles.sale.payment.adapter.out.stone;

import dev.kalles.sale.payment.application.port.out.PaymentWebhookPort;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStatus;
import dev.kalles.sale.payment.domain.PaymentWebhookEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

import static dev.kalles.sale.payment.adapter.out.stone.StoneMappingUtils.toPaymentMethodType;

@Component
public class StonePaymentWebhookAdapter implements PaymentWebhookPort {

    private final String webhookSecret;

    public StonePaymentWebhookAdapter(@Value("${stone.webhook-secret:}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.STONE;
    }

    @Override
    public boolean validateSignature(String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }
        if (xSignature == null || xSignature.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                webhookSecret.getBytes(StandardCharsets.UTF_8),
                xSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaymentWebhookEvent parseEvent(Map<String, Object> payload) {
        String type = stringValue(payload.get("type"));
        if (!"charge.paid".equals(type) && !"charge.refunded".equals(type)) {
            return null;
        }

        Map<String, Object> data = mapValue(payload.get("data"));
        if (data == null) {
            return null;
        }

        Map<String, Object> order = mapValue(data.get("order"));
        Map<String, Object> orderMetadata = order == null ? null : mapValue(order.get("metadata"));
        Map<String, Object> providerMetadata = mapValue(data.get("metadata"));

        BigDecimal paidAmount = toAmount(data.get("paid_amount"));
        BigDecimal originalAmount = order == null ? BigDecimal.ZERO : toAmount(order.get("amount"));
        boolean amountDivergence = paidAmount != null
                && originalAmount != null
                && originalAmount.compareTo(BigDecimal.ZERO) > 0
                && paidAmount.compareTo(originalAmount) != 0;

        String fundingSource = metadataValue(providerMetadata, "account_funding_source");
        String paymentMethod = stringValue(data.get("payment_method"));
        PaymentMethodType methodType = toPaymentMethodType(paymentMethod, fundingSource);

        Map<String, Object> eventMetadata = new LinkedHashMap<>();
        eventMetadata.put("webhookType", type);
        eventMetadata.put("paidAmount", paidAmount);
        eventMetadata.put("originalAmount", originalAmount);
        eventMetadata.put("amountDivergence", amountDivergence);
        eventMetadata.put("terminalSerialNumber", firstNonBlank(
                metadataValue(providerMetadata, "terminal_serial_number"),
                metadataValue(orderMetadata, "terminalSerialNumber"),
                metadataValue(orderMetadata, "terminal_serial_number")
        ));
        eventMetadata.put("payload", payload);

        return new PaymentWebhookEvent(
                PaymentProvider.STONE,
                type,
                order == null ? null : stringValue(order.get("id")),
                stringValue(data.get("id")),
                firstNonBlank(
                        metadataValue(orderMetadata, "externalReference"),
                        metadataValue(orderMetadata, "external_reference"),
                        metadataValue(providerMetadata, "externalReference")
                ),
                paidAmount,
                toDomainStatus(type, stringValue(data.get("status"))),
                methodType,
                eventMetadata
        );
    }

    private PaymentStatus toDomainStatus(String webhookType, String providerStatus) {
        if ("charge.refunded".equals(webhookType)) {
            return PaymentStatus.REFUNDED;
        }
        return StoneMappingUtils.toPaymentStatus(providerStatus);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private BigDecimal toAmount(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cents = new BigDecimal(String.valueOf(value));
        return cents.movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String metadataValue(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        return String.valueOf(metadata.get(key));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
