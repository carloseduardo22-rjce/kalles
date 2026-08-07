package dev.kalles.billing.adapter.in.web;

import dev.kalles.billing.application.service.ProcessBillingWebhookUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final ProcessBillingWebhookUseCase processBillingWebhookUseCase;

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader("Stripe-Signature") String signature,
            @RequestBody String payload) {
        processBillingWebhookUseCase.execute(payload, signature);
        return ResponseEntity.ok().build();
    }
}
