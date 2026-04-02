package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.CancelPaymentOrderUseCase;
import dev.kalles.sale.mercadopago.application.usecase.FetchPaymentOrderUseCase;
import dev.kalles.sale.mercadopago.application.usecase.GenerateDynamicQrCodeUseCase;
import dev.kalles.sale.mercadopago.application.usecase.ProcessPaymentOrderUseCase;
import dev.kalles.sale.mercadopago.application.usecase.RefundPaymentOrderUseCase;
import dev.kalles.sale.mercadopago.domain.ResultadoPoint;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/mercadopago/orders")
public class MercadoPagoOrderController {

    private final GenerateDynamicQrCodeUseCase generateDynamicQrCodeUseCase;
    private final ProcessPaymentOrderUseCase processPaymentOrderUseCase;
    private final CancelPaymentOrderUseCase cancelPaymentOrderUseCase;
    private final RefundPaymentOrderUseCase refundPaymentOrderUseCase;
    private final FetchPaymentOrderUseCase fetchPaymentOrderUseCase;

    public MercadoPagoOrderController(
            GenerateDynamicQrCodeUseCase generateDynamicQrCodeUseCase,
            ProcessPaymentOrderUseCase processPaymentOrderUseCase,
            CancelPaymentOrderUseCase cancelPaymentOrderUseCase,
            RefundPaymentOrderUseCase refundPaymentOrderUseCase,
            FetchPaymentOrderUseCase fetchPaymentOrderUseCase) {
        this.generateDynamicQrCodeUseCase = generateDynamicQrCodeUseCase;
        this.processPaymentOrderUseCase = processPaymentOrderUseCase;
        this.cancelPaymentOrderUseCase = cancelPaymentOrderUseCase;
        this.refundPaymentOrderUseCase = refundPaymentOrderUseCase;
        this.fetchPaymentOrderUseCase = fetchPaymentOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderQrResponse> createOrder(@RequestBody CreateOrderRequest request) {
        String idempotencyKey = request.idempotencyKey() != null && !request.idempotencyKey().isBlank()
                ? request.idempotencyKey()
                : UUID.randomUUID().toString();

        ResultadoQr resultadoQr = generateDynamicQrCodeUseCase.execute(
                request.pedidoIdErp(),
                request.amount(),
                request.caixaId(),
                idempotencyKey
        );

        return ResponseEntity.ok(new OrderQrResponse(resultadoQr.orderId(), resultadoQr.qrData()));
    }

    @PostMapping("/point")
    public ResponseEntity<ResultadoPoint> createPointOrder(@RequestBody CreatePointOrderRequest request) {
        ResultadoPoint result = processPaymentOrderUseCase.startPayment(
                request.terminalId(),
                request.amount(),
                request.description(),
                request.externalReference(),
                request.paymentMethodType()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/point/{orderId}/cancel")
    public ResponseEntity<Void> cancelPointOrder(@PathVariable String orderId) {
        cancelPaymentOrderUseCase.execute(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/point/{orderId}")
    public ResponseEntity<ResultadoPoint> getPointOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(fetchPaymentOrderUseCase.execute(orderId));
    }

    @PostMapping("/point/payments/{paymentId}/refund")
    public ResponseEntity<Void> refundPointPayment(@PathVariable String paymentId) {
        refundPaymentOrderUseCase.execute(paymentId);
        return ResponseEntity.ok().build();
    }

    public record CreatePointOrderRequest(String terminalId, BigDecimal amount, String description, String externalReference, String paymentMethodType) {}

    public record CreateOrderRequest(
            String pedidoIdErp,
            BigDecimal amount,
            String caixaId,
            String idempotencyKey
    ) {}

    public record OrderQrResponse(
            String orderId,
            String qrData
    ) {}
}
