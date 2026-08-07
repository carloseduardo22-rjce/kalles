package dev.kalles.core.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kalles.core.entity.CompanyProduct;

@Repository
public interface CompanyProductRepository extends JpaRepository<CompanyProduct, UUID> {
    Optional<CompanyProduct> findByCompanyIdAndProductId(UUID companyId, UUID productId);
}
