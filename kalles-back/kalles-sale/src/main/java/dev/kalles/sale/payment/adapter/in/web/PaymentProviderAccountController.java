package dev.kalles.sale.payment.adapter.in.web;

import dev.kalles.sale.payment.adapter.in.web.dto.LinkPaymentProviderAccountRequest;
import dev.kalles.sale.payment.adapter.in.web.dto.PaymentProviderLinkStatusResponse;
import dev.kalles.sale.payment.application.port.in.GetPaymentProviderAccountStatusUseCase;
import dev.kalles.sale.payment.application.port.in.LinkPaymentProviderAccountUseCase;
import dev.kalles.sale.payment.domain.PaymentProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-providers")
public class PaymentProviderAccountController {

    private final LinkPaymentProviderAccountUseCase linkPaymentProviderAccountUseCase;
    private final GetPaymentProviderAccountStatusUseCase getPaymentProviderAccountStatusUseCase;

    public PaymentProviderAccountController(
            LinkPaymentProviderAccountUseCase linkPaymentProviderAccountUseCase,
            GetPaymentProviderAccountStatusUseCase getPaymentProviderAccountStatusUseCase
    ) {
        this.linkPaymentProviderAccountUseCase = linkPaymentProviderAccountUseCase;
        this.getPaymentProviderAccountStatusUseCase = getPaymentProviderAccountStatusUseCase;
    }

    @PostMapping("/link")
    public ResponseEntity<Void> linkAccount(@Valid @RequestBody LinkPaymentProviderAccountRequest request) {
        linkPaymentProviderAccountUseCase.execute(request.toCommand());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{provider}/status")
    public ResponseEntity<PaymentProviderLinkStatusResponse> getStatus(@PathVariable PaymentProvider provider) {
        return ResponseEntity.ok(
                new PaymentProviderLinkStatusResponse(provider, getPaymentProviderAccountStatusUseCase.execute(provider))
        );
    }
}
