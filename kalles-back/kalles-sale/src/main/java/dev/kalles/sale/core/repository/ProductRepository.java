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
		"p.id, p.name, p.internalCode, p.barcode, COALESCE(cp.price, 0.0), p.description, COALESCE(cp.active, false), " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN CompanyProduct cp ON cp.product = p AND cp.companyId = :companyId " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"WHERE cp.active = true AND cp.companyId = :companyId " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, cp.price, p.description, cp.active " +
		"ORDER BY p.name ASC")
	List<ProductResponse> findAllActiveWithStock(@Param("companyId") UUID companyId);

	@Query("SELECT new dev.kalles.sale.core.dto.ProductResponse(" +
		"p.id, p.name, p.internalCode, p.barcode, COALESCE(cp.price, 0.0), p.description, COALESCE(cp.active, false), " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN CompanyProduct cp ON cp.product = p AND cp.companyId = :companyId " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, cp.price, p.description, cp.active " +
		"ORDER BY p.name ASC")
	List<ProductResponse> findAllWithStock(@Param("companyId") UUID companyId);

	@Query("SELECT new dev.kalles.sale.core.dto.ProductResponse(" +
		"p.id, p.name, p.internalCode, p.barcode, COALESCE(cp.price, 0.0), p.description, COALESCE(cp.active, false), " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN CompanyProduct cp ON cp.product = p AND cp.companyId = :companyId " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"WHERE cp.active = true AND cp.companyId = :companyId AND (" +
		"LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
		"LOWER(p.internalCode) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
		"LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :q, '%'))" +
		") " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, cp.price, p.description, cp.active " +
		"ORDER BY p.name ASC")
	List<ProductResponse> searchActiveProductsWithStock(@Param("q") String q, @Param("companyId") UUID companyId);
	@Query("SELECT new dev.kalles.sale.core.dto.ProductResponse(" +
		"p.id, p.name, p.internalCode, p.barcode, COALESCE(cp.price, 0.0), p.description, COALESCE(cp.active, false), " +
		"SUM(s.quantity), " +
		"MAX(w.name), " +
		"MAX(l.code)) " +
		"FROM Product p " +
		"LEFT JOIN CompanyProduct cp ON cp.product = p AND cp.companyId = :companyId " +
		"LEFT JOIN Stock s ON s.product = p " +
		"LEFT JOIN s.location l " +
		"LEFT JOIN l.warehouse w " +
		"WHERE p.id = :id " +
		"GROUP BY p.id, p.name, p.internalCode, p.barcode, cp.price, p.description, cp.active")
	Optional<ProductResponse> findProductWithStockById(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
