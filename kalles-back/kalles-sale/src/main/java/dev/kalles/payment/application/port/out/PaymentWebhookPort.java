package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentWebhookEvent;

import java.util.Map;

public interface PaymentWebhookPort extends ProviderAwarePort {

    boolean validateSignature(String xSignature, String xRequestId, String dataId);

    PaymentWebhookEvent parseEvent(Map<String, Object> payload);
}
