package dev.kalles.billing.adapter.in.web;

import dev.kalles.billing.application.port.out.BillingGateway;
import dev.kalles.billing.application.service.CreateBillingCheckoutSessionUseCase;
import dev.kalles.billing.application.service.CreateBillingPortalSessionUseCase;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.entity.Account;
import dev.kalles.security.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final CreateBillingCheckoutSessionUseCase createBillingCheckoutSessionUseCase;
    private final CreateBillingPortalSessionUseCase createBillingPortalSessionUseCase;
    private final AccountRepository accountRepository;

    @PostMapping("/checkout-sessions")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            org.springframework.security.core.Authentication authentication,
            @RequestBody CreateCheckoutSessionRequest request) {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null || authentication == null) {
            return ResponseEntity.status(401).build();
        }

        var account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada nao encontrada."));

        BillingGateway.CheckoutSession checkoutSession = createBillingCheckoutSessionUseCase.execute(
                tenantId,
                account.getEmail(),
                account.getName(),
                request.returnUrl()
        );

        return ResponseEntity.ok(new CheckoutSessionResponse(
                checkoutSession.sessionId(),
                checkoutSession.clientSecret(),
                checkoutSession.url(),
                checkoutSession.status().name()
        ));
    }

    @PostMapping("/portal-sessions")
    public ResponseEntity<PortalSessionResponse> createPortalSession(@RequestBody CreatePortalSessionRequest request) {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        BillingGateway.PortalSession portalSession = createBillingPortalSessionUseCase.execute(tenantId, request.returnUrl());
        return ResponseEntity.ok(new PortalSessionResponse(portalSession.url()));
    }

    public record CreateCheckoutSessionRequest(String returnUrl) {}

    public record CheckoutSessionResponse(
            String sessionId,
            String clientSecret,
            String url,
            String status
    ) {}

    public record CreatePortalSessionRequest(String returnUrl) {}

    public record PortalSessionResponse(String url) {}
}
