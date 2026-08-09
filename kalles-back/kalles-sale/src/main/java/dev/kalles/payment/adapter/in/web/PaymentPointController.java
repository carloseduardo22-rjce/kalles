package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.adapter.in.web.dto.CreatePaymentPointRequest;
import dev.kalles.payment.adapter.in.web.dto.CreatePaymentPointResponse;
import dev.kalles.payment.adapter.in.web.dto.PaymentPointResponse;
import dev.kalles.payment.application.port.in.CreatePaymentPointUseCase;
import dev.kalles.payment.application.port.in.ListPaymentPointsUseCase;
import dev.kalles.payment.domain.PaymentProvider;
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
@RequestMapping("/api/payment-points")
public class PaymentPointController {

    private final CreatePaymentPointUseCase createPaymentPointUseCase;
    private final ListPaymentPointsUseCase listPaymentPointsUseCase;

    public PaymentPointController(
            CreatePaymentPointUseCase createPaymentPointUseCase,
            ListPaymentPointsUseCase listPaymentPointsUseCase
    ) {
        this.createPaymentPointUseCase = createPaymentPointUseCase;
        this.listPaymentPointsUseCase = listPaymentPointsUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatePaymentPointResponse> createPoint(@Valid @RequestBody CreatePaymentPointRequest request) {
        return ResponseEntity.ok(CreatePaymentPointResponse.from(createPaymentPointUseCase.execute(request.toCommand())));
    }

    @GetMapping
    public ResponseEntity<List<PaymentPointResponse>> listPoints(@RequestParam PaymentProvider provider) {
        return ResponseEntity.ok(
                listPaymentPointsUseCase.execute(provider)
                        .stream()
                        .map(PaymentPointResponse::from)
                        .toList()
        );
    }
}
