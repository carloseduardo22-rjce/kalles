package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Stock s " +
           "JOIN s.location l JOIN l.warehouse w " +
           "WHERE s.product.id = :productId AND w.companyId = :companyId")
    int sumQuantityByProductId(@Param("productId") UUID productId, @Param("companyId") UUID companyId);

    Optional<Stock> findByProductIdAndLocationId(UUID productId, UUID locationId);

    @Query("SELECT s FROM Stock s " +
           "JOIN s.location l JOIN l.warehouse w " +
           "WHERE s.product.id = :productId AND w.companyId = :companyId " +
           "ORDER BY s.quantity DESC")
    List<Stock> findAllByProductIdOrderByQuantityDesc(@Param("productId") UUID productId, @Param("companyId") UUID companyId);

    List<Stock> findAllByLocationId(UUID locationId);
}
