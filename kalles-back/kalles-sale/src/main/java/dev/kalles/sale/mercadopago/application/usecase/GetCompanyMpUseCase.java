package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetCompanyMpUseCase {

    private final CompanyMpRepository companyMpRepository;

    public GetCompanyMpUseCase(CompanyMpRepository companyMpRepository) {
        this.companyMpRepository = companyMpRepository;
    }

    public Optional<Company> execute(String externalId) {
        return companyMpRepository.findByExternalId(externalId);
    }
}
