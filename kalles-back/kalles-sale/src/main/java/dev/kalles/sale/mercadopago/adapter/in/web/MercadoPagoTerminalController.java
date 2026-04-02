package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.ActivatePdvModeUseCase;
import dev.kalles.sale.mercadopago.application.usecase.FetchMercadoPagoTerminalsUseCase;
import dev.kalles.sale.mercadopago.domain.Terminal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mercadopago/terminals")
public class MercadoPagoTerminalController {

    private final FetchMercadoPagoTerminalsUseCase fetchMercadoPagoTerminalsUseCase;
    private final ActivatePdvModeUseCase activatePdvModeUseCase;

    public MercadoPagoTerminalController(FetchMercadoPagoTerminalsUseCase fetchMercadoPagoTerminalsUseCase, ActivatePdvModeUseCase activatePdvModeUseCase) {
        this.fetchMercadoPagoTerminalsUseCase = fetchMercadoPagoTerminalsUseCase;
        this.activatePdvModeUseCase = activatePdvModeUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Terminal>> fetchTerminals(@RequestParam UUID storeId, @RequestParam UUID posId) {
        return ResponseEntity.ok(fetchMercadoPagoTerminalsUseCase.execute(storeId, posId));
    }

    @PostMapping("/activate-pdv")
    public ResponseEntity<Void> activatePdvMode(@RequestBody ActivatePdvRequest request) {
        activatePdvModeUseCase.execute(request.storeId(), request.posId(), request.terminalSerial());
        return ResponseEntity.ok().build();
    }

    public record ActivatePdvRequest(UUID storeId, UUID posId, String terminalSerial) {}
}
