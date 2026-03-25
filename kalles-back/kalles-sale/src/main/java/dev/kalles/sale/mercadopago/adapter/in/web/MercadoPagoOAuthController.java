package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.LinkMercadoPagoAccountUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/mercadopago/oauth")
public class MercadoPagoOAuthController {

    private final LinkMercadoPagoAccountUseCase linkUseCase;

    public MercadoPagoOAuthController(LinkMercadoPagoAccountUseCase linkUseCase) {
        this.linkUseCase = linkUseCase;
    }

    @PostMapping("/link")
    public ResponseEntity<Void> linkAccount(@RequestBody LinkAccountRequest request) {
        // The 'state' typically maps to the tenant/company ID to know whose account is linking
        linkUseCase.execute(request.code(), request.state());
        return ResponseEntity.ok().build();
    }

    public record LinkAccountRequest(String code, String state) {}
}