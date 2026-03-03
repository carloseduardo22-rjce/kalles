package dev.kalles.sale.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.kalles.sale.core.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
	Optional<Product> findByInternalCode(String internalCode);
	Optional<Product> findByBarcode(String barcode);
	List<Product> findByDescriptionContainingIgnoreCaseAndActiveTrue(String description);
}
