package dev.kalles.sale.core.service;

import dev.kalles.sale.core.entity.Company;
import dev.kalles.sale.core.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company createCompany(Company company) {
        // You might want to assign a default/random external ID if needed, 
        // but it is handled in DTO mapping.
        return companyRepository.save(company);
    }

    public List<Company> listCompaniesByTenant(UUID tenantId) {
        return companyRepository.findByTenantId(tenantId);
    }

    public List<Company> listCompaniesByTenantAndOptionalCompany(UUID tenantId, UUID companyId) {
        if (companyId != null) {
            return companyRepository.findByIdAndTenantId(companyId, tenantId)
                    .map(List::of)
                    .orElseGet(List::of);
        }

        return listCompaniesByTenant(tenantId);
    }

    public Company getCompanyById(UUID id) {
        return companyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }
}
