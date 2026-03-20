package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import dev.kalles.sale.mercadopago.port.MercadoPagoStorePort;
import org.springframework.stereotype.Service;

@Service
public class CreateMercadoPagoStoreUseCase {

    private final CompanyMpRepository companyMpRepository;
    private final MercadoPagoStorePort mercadoPagoStorePort;

    public CreateMercadoPagoStoreUseCase(CompanyMpRepository companyMpRepository,
            MercadoPagoStorePort mercadoPagoStorePort) {
        this.companyMpRepository = companyMpRepository;
        this.mercadoPagoStorePort = mercadoPagoStorePort;
    }

    public Long execute(Company companyInfo) {
        Company company = companyMpRepository.findByExternalId(companyInfo.externalId())
                .orElseGet(() -> {
                    companyMpRepository.save(companyInfo);
                    // fetch it again after save to get the generated ID
                    return companyMpRepository.findByExternalId(companyInfo.externalId()).orElseThrow();
                });

        if (company.hasStoreRegistered()) {
            return company.mpStoreId();
        }

        Long storeId = mercadoPagoStorePort.createStore(company);
        companyMpRepository.saveStoreId(company.id(), storeId);

        return storeId;
    }
}
