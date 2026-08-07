package dev.kalles.product.repository;

import java.util.Optional;
import java.util.UUID;

import dev.kalles.product.entity.CompanyProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyProductRepository extends JpaRepository<CompanyProduct, UUID> {
    Optional<CompanyProduct> findByCompanyIdAndProductId(UUID companyId, UUID productId);
}
