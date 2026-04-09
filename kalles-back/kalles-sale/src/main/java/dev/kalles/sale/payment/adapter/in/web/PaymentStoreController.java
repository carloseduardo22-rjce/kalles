package dev.kalles.sale.payment.adapter.in.web;

import dev.kalles.sale.payment.adapter.in.web.dto.CreatePaymentStoreRequest;
import dev.kalles.sale.payment.adapter.in.web.dto.CreatePaymentStoreResponse;
import dev.kalles.sale.payment.adapter.in.web.dto.PaymentStoreResponse;
import dev.kalles.sale.payment.adapter.in.web.dto.PaymentStoreStatusResponse;
import dev.kalles.sale.payment.application.port.in.CreatePaymentStoreUseCase;
import dev.kalles.sale.payment.application.port.in.GetPaymentStoreStatusUseCase;
import dev.kalles.sale.payment.application.port.in.ListPaymentStoresUseCase;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment-stores")
public class PaymentStoreController {

    private final CreatePaymentStoreUseCase createPaymentStoreUseCase;
    private final GetPaymentStoreStatusUseCase getPaymentStoreStatusUseCase;
    private final ListPaymentStoresUseCase listPaymentStoresUseCase;

    public PaymentStoreController(
            CreatePaymentStoreUseCase createPaymentStoreUseCase,
            GetPaymentStoreStatusUseCase getPaymentStoreStatusUseCase,
            ListPaymentStoresUseCase listPaymentStoresUseCase
    ) {
        this.createPaymentStoreUseCase = createPaymentStoreUseCase;
        this.getPaymentStoreStatusUseCase = getPaymentStoreStatusUseCase;
        this.listPaymentStoresUseCase = listPaymentStoresUseCase;
    }

    @GetMapping("/{provider}/{externalReference}/status")
    public ResponseEntity<PaymentStoreStatusResponse> getStoreStatus(
            @PathVariable PaymentProvider provider,
            @PathVariable String externalReference
    ) {
        return ResponseEntity.ok(toStatusResponse(
                provider,
                externalReference,
                getPaymentStoreStatusUseCase.findByExternalReference(provider, externalReference)
        ));
    }

    @GetMapping("/{provider}/my-status")
    public ResponseEntity<PaymentStoreStatusResponse> getCurrentTenantStoreStatus(@PathVariable PaymentProvider provider) {
        return ResponseEntity.ok(toStatusResponse(
                provider,
                null,
                getPaymentStoreStatusUseCase.findCurrentTenant(provider)
        ));
    }

    @GetMapping
    public ResponseEntity<List<PaymentStoreResponse>> listStores(@RequestParam PaymentProvider provider) {
        return ResponseEntity.ok(
                listPaymentStoresUseCase.execute(provider)
                        .stream()
                        .map(PaymentStoreResponse::from)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<CreatePaymentStoreResponse> createStore(@Valid @RequestBody CreatePaymentStoreRequest request) {
        return ResponseEntity.ok(CreatePaymentStoreResponse.from(createPaymentStoreUseCase.execute(request.toCommand())));
    }

    private PaymentStoreStatusResponse toStatusResponse(
            PaymentProvider provider,
            String externalReference,
            Optional<PaymentStore> store
    ) {
        return new PaymentStoreStatusResponse(
                provider,
                store.isPresent(),
                store.map(PaymentStore::hasProviderStore).orElse(false),
                externalReference,
                store.map(PaymentStore::providerStoreId).orElse(null)
        );
    }
}
