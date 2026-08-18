package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.application.port.out.PaymentWebhookPort;
import dev.kalles.payment.config.MercadoPagoProperties;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentWebhookEvent;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toPaymentMethodType;
import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toPaymentStatus;

@Component
public class MercadoPagoPaymentWebhookAdapter implements PaymentWebhookPort {

    private final String webhookSecret;

    public MercadoPagoPaymentWebhookAdapter(MercadoPagoProperties mercadoPagoProperties) {
        this.webhookSecret = mercadoPagoProperties.webhookSecret();
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public boolean validateSignature(String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }

        try {
            String[] parts = xSignature.split(",");
            String ts = null;
            String hash = null;

            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    if ("ts".equals(keyValue[0].trim())) {
                        ts = keyValue[1].trim();
                    } else if ("v1".equals(keyValue[0].trim())) {
                        hash = keyValue[1].trim();
                    }
                }
            }

            if (ts == null || hash == null) {
                return false;
            }

            String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId.toLowerCase(Locale.ROOT), xRequestId, ts);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hmacBytes = hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            return MessageDigest.isEqual(
                    HexFormat.of().formatHex(hmacBytes).getBytes(StandardCharsets.UTF_8),
                    hash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaymentWebhookEvent parseEvent(Map<String, Object> payload) {
        String action = (String) payload.get("action");
        if (action == null) {
            return null;
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            return null;
        }

        String type = (String) data.get("type");
        if ("point".equals(type)) {
            return parsePointEvent(action, data, payload);
        }

        if ("order.processed".equals(action)) {
            return parseLegacyQrEvent(action, data, payload);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private PaymentWebhookEvent parsePointEvent(String action, Map<String, Object> data, Map<String, Object> payload) {
        String providerOrderId = (String) data.get("id");
        String externalReference = (String) data.get("external_reference");
        String status = (String) data.get("status");
        BigDecimal amount = toBigDecimal(data.get("total_paid_amount"));

        String providerPaymentId = null;
        PaymentMethodType methodType = PaymentMethodType.UNSPECIFIED;

        Map<String, Object> transactions = (Map<String, Object>) data.get("transactions");
        if (transactions != null) {
            List<Map<String, Object>> payments = (List<Map<String, Object>>) transactions.get("payments");
            if (payments != null && !payments.isEmpty()) {
                Map<String, Object> payment = payments.get(0);
                Object paymentId = payment.get("id");
                if (paymentId != null) {
                    providerPaymentId = String.valueOf(paymentId);
                }

                Map<String, Object> paymentMethod = (Map<String, Object>) payment.get("payment_method");
                if (paymentMethod != null) {
                    methodType = toPaymentMethodType((String) paymentMethod.get("type"));
                }
            }
        }

        return new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                action,
                providerOrderId,
                providerPaymentId,
                externalReference,
                amount,
                toPaymentStatus(status),
                methodType,
                payload
        );
    }

    private PaymentWebhookEvent parseLegacyQrEvent(String action, Map<String, Object> data, Map<String, Object> payload) {
        return new PaymentWebhookEvent(
                PaymentProvider.MERCADO_PAGO,
                action,
                data.get("id") != null ? String.valueOf(data.get("id")) : null,
                null,
                (String) data.get("external_reference"),
                toBigDecimal(data.get("total_paid_amount")),
                toPaymentStatus((String) data.get("status")),
                PaymentMethodType.UNSPECIFIED,
                payload
        );
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
