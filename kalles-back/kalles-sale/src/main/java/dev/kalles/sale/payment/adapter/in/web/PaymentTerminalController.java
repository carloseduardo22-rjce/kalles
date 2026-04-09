package dev.kalles.sale.payment.adapter.in.web;

import dev.kalles.sale.payment.adapter.in.web.dto.ActivatePaymentTerminalRequest;
import dev.kalles.sale.payment.adapter.in.web.dto.PaymentTerminalResponse;
import dev.kalles.sale.payment.application.port.in.ActivatePaymentTerminalUseCase;
import dev.kalles.sale.payment.application.port.in.ListPaymentTerminalsUseCase;
import dev.kalles.sale.payment.application.port.in.command.ListPaymentTerminalsQuery;
import dev.kalles.sale.payment.domain.PaymentProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment-terminals")
public class PaymentTerminalController {

    private final ListPaymentTerminalsUseCase listPaymentTerminalsUseCase;
    private final ActivatePaymentTerminalUseCase activatePaymentTerminalUseCase;

    public PaymentTerminalController(
            ListPaymentTerminalsUseCase listPaymentTerminalsUseCase,
            ActivatePaymentTerminalUseCase activatePaymentTerminalUseCase
    ) {
        this.listPaymentTerminalsUseCase = listPaymentTerminalsUseCase;
        this.activatePaymentTerminalUseCase = activatePaymentTerminalUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PaymentTerminalResponse>> listTerminals(
            @RequestParam PaymentProvider provider,
            @RequestParam String storeId,
            @RequestParam String pointId
    ) {
        return ResponseEntity.ok(
                listPaymentTerminalsUseCase.execute(new ListPaymentTerminalsQuery(provider, storeId, pointId))
                        .stream()
                        .map(PaymentTerminalResponse::from)
                        .toList()
        );
    }

    @PostMapping("/activate-point-of-sale")
    public ResponseEntity<Void> activatePointOfSale(@Valid @RequestBody ActivatePaymentTerminalRequest request) {
        activatePaymentTerminalUseCase.execute(request.toCommand());
        return ResponseEntity.ok().build();
    }
}
