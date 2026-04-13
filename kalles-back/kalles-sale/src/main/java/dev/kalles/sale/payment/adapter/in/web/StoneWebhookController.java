package dev.kalles.sale.payment.adapter.in.web;

import dev.kalles.sale.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.sale.payment.domain.PaymentProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/stone")
public class StoneWebhookController {

    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

    public StoneWebhookController(ProcessPaymentWebhookUseCase processPaymentWebhookUseCase) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> receiveNotification(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestHeader(value = "x-data-id", required = false) String dataId,
            @RequestBody Map<String, Object> payload
    ) {
        boolean isValid = processPaymentWebhookUseCase.validateSignature(
                PaymentProvider.STONE,
                xSignature,
                xRequestId,
                dataId
        );
        if (!isValid) {
            return ResponseEntity.status(403).build();
        }

        boolean processed = processPaymentWebhookUseCase.execute(PaymentProvider.STONE, payload);
        return processed ? ResponseEntity.ok().build() : ResponseEntity.accepted().build();
    }
}
