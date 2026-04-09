package dev.kalles.sale.payment.adapter.in.web;

import dev.kalles.sale.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.sale.payment.domain.PaymentProvider;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> receiveNotification(@RequestBody Map<String, Object> payload) {
        boolean processed = processPaymentWebhookUseCase.execute(PaymentProvider.STONE, payload);
        return processed ? ResponseEntity.ok().build() : ResponseEntity.accepted().build();
    }
}
