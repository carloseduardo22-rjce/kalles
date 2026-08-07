package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.adapter.in.web.dto.ClosePaymentOrderRequest;
import dev.kalles.payment.adapter.in.web.dto.PaymentResponse;
import dev.kalles.payment.adapter.in.web.dto.PrintPaymentDocumentRequest;
import dev.kalles.payment.adapter.in.web.dto.ProcessPaymentRequest;
import dev.kalles.payment.application.port.in.ClosePaymentOrderUseCase;
import dev.kalles.payment.application.port.in.CancelPaymentUseCase;
import dev.kalles.payment.application.port.in.GetPaymentUseCase;
import dev.kalles.payment.application.port.in.PrintPaymentDocumentUseCase;
import dev.kalles.payment.application.port.in.ProcessPaymentUseCase;
import dev.kalles.payment.application.port.in.RefundPaymentUseCase;
import dev.kalles.payment.domain.PaymentProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final ClosePaymentOrderUseCase closePaymentOrderUseCase;
    private final PrintPaymentDocumentUseCase printPaymentDocumentUseCase;
    private final RefundPaymentUseCase refundPaymentUseCase;

    public PaymentController(
            ProcessPaymentUseCase processPaymentUseCase,
            GetPaymentUseCase getPaymentUseCase,
            CancelPaymentUseCase cancelPaymentUseCase,
            ClosePaymentOrderUseCase closePaymentOrderUseCase,
            PrintPaymentDocumentUseCase printPaymentDocumentUseCase,
            RefundPaymentUseCase refundPaymentUseCase
    ) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.cancelPaymentUseCase = cancelPaymentUseCase;
        this.closePaymentOrderUseCase = closePaymentOrderUseCase;
        this.printPaymentDocumentUseCase = printPaymentDocumentUseCase;
        this.refundPaymentUseCase = refundPaymentUseCase;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> process(@Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(PaymentResponse.from(request.provider(), processPaymentUseCase.execute(request.toCommand())));
    }

    @GetMapping("/{provider}/{providerOrderId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable PaymentProvider provider,
            @PathVariable String providerOrderId
    ) {
        return ResponseEntity.ok(PaymentResponse.from(provider, getPaymentUseCase.execute(provider, providerOrderId)));
    }

    @PostMapping("/{provider}/{providerOrderId}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable PaymentProvider provider,
            @PathVariable String providerOrderId
    ) {
        cancelPaymentUseCase.execute(provider, providerOrderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{provider}/{providerOrderId}/close")
    public ResponseEntity<Void> closePaymentOrder(
            @PathVariable PaymentProvider provider,
            @PathVariable String providerOrderId,
            @Valid @RequestBody ClosePaymentOrderRequest request
    ) {
        closePaymentOrderUseCase.execute(provider, providerOrderId, request.status());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{provider}/{providerOrderId}/documents/print")
    public ResponseEntity<Void> printDocument(
            @PathVariable PaymentProvider provider,
            @PathVariable String providerOrderId,
            @Valid @RequestBody PrintPaymentDocumentRequest request
    ) {
        printPaymentDocumentUseCase.execute(provider, providerOrderId, request.toCommand());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{provider}/payments/{providerPaymentId}/refund")
    public ResponseEntity<Void> refundPayment(
            @PathVariable PaymentProvider provider,
            @PathVariable String providerPaymentId
    ) {
        refundPaymentUseCase.execute(provider, providerPaymentId);
        return ResponseEntity.ok().build();
    }
}
