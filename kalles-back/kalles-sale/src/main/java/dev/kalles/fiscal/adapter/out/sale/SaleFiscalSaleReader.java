package dev.kalles.fiscal.adapter.out.sale;

import dev.kalles.company.entity.Company;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.fiscal.application.port.out.FiscalProductClassificationRepository;
import dev.kalles.fiscal.application.port.out.FiscalSaleReader;
import dev.kalles.fiscal.domain.FiscalSale;
import dev.kalles.fiscal.domain.FiscalSaleItem;
import dev.kalles.fiscal.domain.FiscalSaleStatus;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SaleFiscalSaleReader implements FiscalSaleReader {

    private final SaleRepository saleRepository;
    private final CompanyRepository companyRepository;
    private final FiscalProductClassificationRepository classificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<FiscalSale> findByIdForTenantAndCompany(UUID saleId, UUID tenantId, UUID companyId) {
        return saleRepository.findById(saleId)
                .filter(sale -> companyId.equals(sale.getCompanyId()))
                .filter(sale -> companyRepository.findById(sale.getCompanyId())
                        .map(Company::getTenantId)
                        .filter(tenantId::equals)
                        .isPresent())
                .map(sale -> toFiscalSale(sale, tenantId));
    }

    private FiscalSale toFiscalSale(Sale sale, UUID tenantId) {
        var items = sale.getItems().stream()
                .map(item -> {
                    var classification = classificationRepository
                            .findByProduct(tenantId, sale.getCompanyId(), item.getProduct().getId());
                    return new FiscalSaleItem(
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            classification.map(dev.kalles.fiscal.domain.FiscalProductClassification::ncm).orElse(null),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getSubtotal()
                    );
                })
                .toList();

        return new FiscalSale(
                sale.getId(),
                tenantId,
                sale.getCompanyId(),
                toFiscalStatus(sale.getStateName()),
                sale.getTotal(),
                items
        );
    }

    private FiscalSaleStatus toFiscalStatus(String stateName) {
        try {
            return FiscalSaleStatus.valueOf(stateName);
        } catch (IllegalArgumentException ex) {
            return FiscalSaleStatus.OPEN;
        }
    }
}
