package dev.kalles.sale.api.company;

import dev.kalles.sale.core.entity.Company;
import dev.kalles.sale.core.service.CompanyService;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@RequestBody CreateCompanyRequest request) {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        Company company = new Company();
        company.setName(request.name());
        company.setStreetName(request.streetName());
        company.setStreetNumber(request.streetNumber());
        company.setCityName(request.cityName());
        company.setStateName(request.stateName());
        company.setLatitude(request.latitude());
        company.setLongitude(request.longitude());
        company.setTenantId(tenantId);

        Company saved = companyService.createCompany(company);

        return ResponseEntity.ok(new CompanyResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> listMyCompanies() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        List<CompanyResponse> responses = companyService.listCompaniesByTenant(tenantId)
                .stream()
                .map(CompanyResponse::new)
                .toList();

        return ResponseEntity.ok(responses);
    }

    public record CreateCompanyRequest(
            String name,
            String streetName,
            String streetNumber,
            String cityName,
            String stateName,
            Double latitude,
            Double longitude
    ) {}

    public record CompanyResponse(
            UUID id,
            String name,
            String streetName,
            String streetNumber,
            String cityName,
            String stateName,
            Double latitude,
            Double longitude
    ) {
        public CompanyResponse(Company company) {
            this(
                company.getId(),
                company.getName(),
                company.getStreetName(),
                company.getStreetNumber(),
                company.getCityName(),
                company.getStateName(),
                company.getLatitude(),
                company.getLongitude()
            );
        }
    }
}
