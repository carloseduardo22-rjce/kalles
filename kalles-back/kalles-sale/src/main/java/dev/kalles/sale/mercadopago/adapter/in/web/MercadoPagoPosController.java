package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.CreateMercadoPagoPosUseCase;
import dev.kalles.sale.mercadopago.domain.Caixa;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mercadopago/pos")
public class MercadoPagoPosController {

    private final CreateMercadoPagoPosUseCase createMercadoPagoPosUseCase;

    public MercadoPagoPosController(CreateMercadoPagoPosUseCase createMercadoPagoPosUseCase) {
        this.createMercadoPagoPosUseCase = createMercadoPagoPosUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatePosResponse> createPos(@RequestBody CreatePosRequest request) {
        Caixa caixa = new Caixa(request.caixaId(), request.name(), request.companyId(), null);
        Long posId = createMercadoPagoPosUseCase.execute(caixa);
        return ResponseEntity.ok(new CreatePosResponse(posId));
    }

    public record CreatePosRequest(String caixaId, String name, String companyId) {}
    public record CreatePosResponse(Long posId) {}
}
