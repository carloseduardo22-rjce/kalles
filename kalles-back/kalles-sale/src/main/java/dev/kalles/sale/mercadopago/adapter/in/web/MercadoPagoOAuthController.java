package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.LinkMercadoPagoAccountUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import dev.kalles.sale.mercadopago.port.TenantRepository;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/mercadopago/oauth")
public class MercadoPagoOAuthController {

    private final LinkMercadoPagoAccountUseCase linkUseCase;
    private final TenantRepository tenantRepository;

    public MercadoPagoOAuthController(LinkMercadoPagoAccountUseCase linkUseCase, TenantRepository tenantRepository) {
        this.linkUseCase = linkUseCase;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping("/link")
    public ResponseEntity<Void> linkAccount(@RequestBody LinkAccountRequest request) {
        // The 'state' typically maps to the tenant/company ID to know whose account is
        // linking
        linkUseCase.execute(request.code(), request.state());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<OAuthStatusResponse> getStatus() {
        java.util.UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return tenantRepository.findById(tenantId)
                .map(tenant -> ResponseEntity.ok(new OAuthStatusResponse(tenant.mpAccessToken() != null)))
                .orElseGet(() -> ResponseEntity.ok(new OAuthStatusResponse(false)));
    }

    public record LinkAccountRequest(String code, String state) {
    }

    public record OAuthStatusResponse(boolean isLinked) {
    }
}
