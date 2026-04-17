package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByTenantId(UUID tenantId);
    Optional<Company> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
