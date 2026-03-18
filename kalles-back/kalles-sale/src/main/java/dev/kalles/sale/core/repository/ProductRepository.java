package dev.kalles.sale.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.kalles.sale.core.dto.ProductResponse;
import dev.kalles.sale.core.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
	Optional<Product> findByInternalCode(String internalCode);
	Optional<Product> findByBarcode(String barcode);
	List<Product> findByDescriptionContainingIgnoreCaseAndActiveTrue(String description);

	@Query("SELECT new dev.kalles.sale.core.dto.ProductResponse(" +
		"p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active, " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"WHERE p.active = true " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active " +
		"ORDER BY p.name ASC")
	List<ProductResponse> findAllActiveWithStock();

	@Query("SELECT new dev.kalles.sale.core.dto.ProductResponse(" +
		"p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active, " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active " +
		"ORDER BY p.name ASC")
	List<ProductResponse> findAllWithStock();

	@Query("SELECT new dev.kalles.sale.core.dto.ProductResponse(" +
		"p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active, " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"WHERE p.active = true AND (" +
		"LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
		"LOWER(p.internalCode) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
		"LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :q, '%'))" +
		") " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active " +
		"ORDER BY p.name ASC")
	List<ProductResponse> searchActiveProductsWithStock(@Param("q") String q);
	@Query("SELECT new dev.kalles.sale.core.dto.ProductResponse(" +
		"p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active, " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"WHERE p.id = :id " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, p.price, p.description, p.active")
	Optional<ProductResponse> findProductWithStockById(@Param("id") UUID id);
}
