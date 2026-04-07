package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import dev.kalles.sale.mercadopago.port.MercadoPagoStorePort;
import dev.kalles.sale.core.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateMercadoPagoStoreUseCase {

    private final CompanyMpRepository companyMpRepository;
    private final MercadoPagoStorePort mercadoPagoStorePort;
    private final CompanyRepository companyRepository;

    public CreateMercadoPagoStoreUseCase(CompanyMpRepository companyMpRepository,
            MercadoPagoStorePort mercadoPagoStorePort, CompanyRepository companyRepository) {
        this.companyMpRepository = companyMpRepository;
        this.mercadoPagoStorePort = mercadoPagoStorePort;
        this.companyRepository = companyRepository;
    }

    public Long execute(Company companyInfo, UUID coreCompanyId) {
        dev.kalles.sale.core.entity.Company coreCompany = companyRepository.findById(coreCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Loja do Core não encontrada!"));

        // Find or create the mapping
        Company company = companyMpRepository.findByExternalId(companyInfo.externalId())
                .orElseGet(() -> {
                    companyMpRepository.save(companyInfo);
                    // fetch it again after save to get the generated ID
                    return companyMpRepository.findByExternalId(companyInfo.externalId()).orElseThrow();
                });

        if (company.hasStoreRegistered()) {
            return company.mpStoreId();
        }

        Long storeId = mercadoPagoStorePort.createStore(company, coreCompany);
        companyMpRepository.saveStoreId(company.id(), storeId);

        return storeId;
    }
}
