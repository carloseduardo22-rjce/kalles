package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.payment.domain.PaymentProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/mercadopago")
public class MercadoPagoWebhookController {

    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;
    private final String webhookSecret;

    public MercadoPagoWebhookController(
            ProcessPaymentWebhookUseCase processPaymentWebhookUseCase,
            @Value("${mercadopago.webhook-secret:}") String webhookSecret
    ) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping
    public ResponseEntity<Void> receiveNotification(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody Map<String, Object> payload
    ) {
        if (webhookSecret.isBlank() || xSignature == null || xRequestId == null || dataId == null) {
            return ResponseEntity.status(403).build();
        }

        boolean isValid = processPaymentWebhookUseCase.validateSignature(
                PaymentProvider.MERCADO_PAGO,
                xSignature,
                xRequestId,
                dataId
        );
        if (!isValid) {
            return ResponseEntity.status(403).build();
        }

        processPaymentWebhookUseCase.execute(PaymentProvider.MERCADO_PAGO, payload);
        return ResponseEntity.ok().build();
    }
}
