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
        Company company = companyMpRepository.findById(companyInfo.id())
                .orElseGet(() -> {
                    companyMpRepository.save(companyInfo);
                    return companyInfo;
                });

        if (company.hasStoreRegistered()) {
            return company.mpStoreId();
        }

        Long storeId = mercadoPagoStorePort.createStore(company);
        companyMpRepository.saveStoreId(company.id(), storeId);

        return storeId;
    }
}
