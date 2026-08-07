package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.adapter.in.web.dto.MapPaymentTerminalRequest;
import dev.kalles.payment.adapter.in.web.dto.PaymentTerminalMappingResponse;
import dev.kalles.payment.application.port.in.GetPaymentTerminalMappingUseCase;
import dev.kalles.payment.application.port.in.ListPaymentTerminalMappingsUseCase;
import dev.kalles.payment.application.port.in.MapPaymentTerminalUseCase;
import dev.kalles.payment.application.port.in.command.GetPaymentTerminalMappingQuery;
import dev.kalles.payment.domain.PaymentProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-terminal-mappings")
public class PaymentTerminalMappingController {

    private final MapPaymentTerminalUseCase mapPaymentTerminalUseCase;
    private final GetPaymentTerminalMappingUseCase getPaymentTerminalMappingUseCase;
    private final ListPaymentTerminalMappingsUseCase listPaymentTerminalMappingsUseCase;

    public PaymentTerminalMappingController(
            MapPaymentTerminalUseCase mapPaymentTerminalUseCase,
            GetPaymentTerminalMappingUseCase getPaymentTerminalMappingUseCase,
            ListPaymentTerminalMappingsUseCase listPaymentTerminalMappingsUseCase
    ) {
        this.mapPaymentTerminalUseCase = mapPaymentTerminalUseCase;
        this.getPaymentTerminalMappingUseCase = getPaymentTerminalMappingUseCase;
        this.listPaymentTerminalMappingsUseCase = listPaymentTerminalMappingsUseCase;
    }

    @PostMapping
    public ResponseEntity<PaymentTerminalMappingResponse> mapTerminal(@Valid @RequestBody MapPaymentTerminalRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentTerminalMappingResponse.from(mapPaymentTerminalUseCase.execute(request.toCommand())));
    }

    @GetMapping
    public ResponseEntity<List<PaymentTerminalMappingResponse>> list(@RequestParam PaymentProvider provider) {
        return ResponseEntity.ok(listPaymentTerminalMappingsUseCase.execute(provider)
                .stream()
                .map(PaymentTerminalMappingResponse::from)
                .toList());
    }

    @GetMapping("/by-cash-register")
    public ResponseEntity<PaymentTerminalMappingResponse> findByCashRegister(
            @RequestParam UUID cashRegisterId,
            @RequestParam PaymentProvider provider
    ) {
        return ResponseEntity.ok(PaymentTerminalMappingResponse.from(
                getPaymentTerminalMappingUseCase.execute(new GetPaymentTerminalMappingQuery(cashRegisterId, provider))
        ));
    }
}
