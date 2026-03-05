package dev.kalles.sale.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.kalles.sale.core.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
	Optional<Product> findByInternalCode(String internalCode);
	Optional<Product> findByBarcode(String barcode);
	List<Product> findByDescriptionContainingIgnoreCaseAndActiveTrue(String description);

	List<Product> findAllByActiveTrueOrderByNameAsc();

	List<Product> findAllByOrderByNameAsc();

	@Query("SELECT p FROM Product p WHERE p.active = true AND (" +
		"LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
		"LOWER(p.internalCode) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
		"LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :q, '%'))" +
		") ORDER BY p.name ASC")
	List<Product> searchActiveProducts(@Param("q") String q);
}
