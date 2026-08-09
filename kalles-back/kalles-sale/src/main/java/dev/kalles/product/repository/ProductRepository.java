package dev.kalles.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.kalles.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

	// Tenant-aware lookups (for uniqueness checks and tenant-scoped operations)
	Optional<Product> findByIdAndTenantId(UUID id, UUID tenantId);
	Optional<Product> findByInternalCodeAndTenantId(String internalCode, UUID tenantId);
	Optional<Product> findByBarcodeAndTenantId(String barcode, UUID tenantId);
	boolean existsByInternalCodeAndTenantId(String internalCode, UUID tenantId);
	boolean existsByBarcodeAndTenantId(String barcode, UUID tenantId);
}
