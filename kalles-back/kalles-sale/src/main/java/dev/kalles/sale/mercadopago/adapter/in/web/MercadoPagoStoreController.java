package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.CreateMercadoPagoStoreUseCase;
import dev.kalles.sale.mercadopago.application.usecase.GetCompanyMpUseCase;
import dev.kalles.sale.mercadopago.domain.Company;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mercadopago/stores")
public class MercadoPagoStoreController {

    private final CreateMercadoPagoStoreUseCase createMercadoPagoStoreUseCase;
    private final GetCompanyMpUseCase getCompanyMpUseCase;

    public MercadoPagoStoreController(CreateMercadoPagoStoreUseCase createMercadoPagoStoreUseCase, GetCompanyMpUseCase getCompanyMpUseCase) {
        this.createMercadoPagoStoreUseCase = createMercadoPagoStoreUseCase;
        this.getCompanyMpUseCase = getCompanyMpUseCase;
    }

    @GetMapping("/{companyId}/status")
    public ResponseEntity<CompanyStatusResponse> getCompanyStoreStatus(@PathVariable UUID companyId) {
        return getCompanyMpUseCase.execute(companyId)
                .map(company -> ResponseEntity.ok(new CompanyStatusResponse(true, company.hasStoreRegistered())))
                .orElseGet(() -> ResponseEntity.ok(new CompanyStatusResponse(false, false)));
    }

    @PostMapping
    public ResponseEntity<CreateStoreResponse> createStore(@RequestBody CreateStoreRequest request) {
        System.out.println(request);
        Company company = new Company(
                request.companyId(),
                request.name(),
                request.streetName(),
                request.streetNumber(),
                request.cityName(),
                request.stateName(),
                request.latitude(),
                request.longitude(),
                null);
        Long storeId = createMercadoPagoStoreUseCase.execute(company);
        return ResponseEntity.ok(new CreateStoreResponse(storeId));
    }

    public record CreateStoreRequest(
            UUID companyId,
            String name,
            String streetName,
            String streetNumber,
            String cityName,
            String stateName,
            double latitude,
            double longitude) {
    }

    public record CreateStoreResponse(Long storeId) {
    }

    public record CompanyStatusResponse(boolean companyExists, boolean hasStoreRegistered) {
    }
}
