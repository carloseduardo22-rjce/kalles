package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.payment.config.MercadoPagoProperties;
import dev.kalles.payment.domain.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/mercadopago")
public class MercadoPagoWebhookController {

    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;
    private final String webhookSecret;

    public MercadoPagoWebhookController(
            ProcessPaymentWebhookUseCase processPaymentWebhookUseCase,
            MercadoPagoProperties mercadoPagoProperties
    ) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
        this.webhookSecret = mercadoPagoProperties.webhookSecret();
    }

    @PostMapping
    public ResponseEntity<Void> receiveNotification(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody Map<String, Object> payload
    ) {
        if (webhookSecret.isBlank()) {
            log.warn("Webhook do Mercado Pago rejeitado: mercadopago.webhook-secret nao esta configurado");
            return ResponseEntity.status(403).build();
        }

        if (xSignature == null || xRequestId == null || dataId == null) {
            log.warn("Webhook do Mercado Pago rejeitado por dados ausentes: x-signature={}, x-request-id={}, data.id={}",
                    xSignature != null, xRequestId != null, dataId != null);
            return ResponseEntity.status(403).build();
        }

        boolean isValid = processPaymentWebhookUseCase.validateSignature(
                PaymentProvider.MERCADO_PAGO,
                xSignature,
                xRequestId,
                dataId
        );
        if (!isValid) {
            log.warn("Webhook do Mercado Pago rejeitado por assinatura invalida: x-request-id={}, data.id={}",
                    xRequestId, dataId);
            return ResponseEntity.status(403).build();
        }

        processPaymentWebhookUseCase.execute(PaymentProvider.MERCADO_PAGO, payload);
        log.info("Webhook do Mercado Pago processado: x-request-id={}, data.id={}", xRequestId, dataId);
        return ResponseEntity.ok().build();
    }
}
