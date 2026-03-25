package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.GenerateDynamicQrCodeUseCase;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

    public MercadoPagoOrderController(GenerateDynamicQrCodeUseCase generateDynamicQrCodeUseCase) {
        this.generateDynamicQrCodeUseCase = generateDynamicQrCodeUseCase;
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
