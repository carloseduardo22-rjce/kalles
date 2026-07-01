package dev.kalles.sale.fiscal.adapter.out.persistence;

import dev.kalles.sale.fiscal.adapter.out.persistence.entity.FiscalCertificateEntity;
import dev.kalles.sale.fiscal.adapter.out.persistence.repository.SpringDataFiscalCertificateRepository;
import dev.kalles.sale.fiscal.application.port.out.FiscalCertificateRepository;
import dev.kalles.sale.fiscal.domain.FiscalCertificate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FiscalCertificatePersistenceAdapter implements FiscalCertificateRepository {

    private final SpringDataFiscalCertificateRepository repository;

    @Override
    public Optional<FiscalCertificate> findActiveByCompany(UUID tenantId, UUID companyId) {
        return repository.findFirstByTenantIdAndCompanyIdAndActiveTrueOrderByExpiresAtDesc(tenantId, companyId)
                .map(FiscalCertificateEntity::toDomain);
    }

    @Override
    public FiscalCertificate save(FiscalCertificate certificate) {
        FiscalCertificateEntity entity = new FiscalCertificateEntity();
        entity.setTenantId(certificate.tenantId());
        entity.setCompanyId(certificate.companyId());
        entity.setExpiresAt(certificate.expiresAt());
        entity.setActive(certificate.active());
        entity.setProtectedContent(certificate.protectedContent());
        entity.setProtectedPassword(certificate.protectedPassword());
        return repository.save(entity).toDomain();
    }

    @Override
    public void deactivateActiveByCompany(UUID tenantId, UUID companyId) {
        var activeCertificates = repository.findAllByTenantIdAndCompanyIdAndActiveTrue(tenantId, companyId);
        activeCertificates.forEach(certificate -> certificate.setActive(false));
        repository.saveAll(activeCertificates);
    }
}
