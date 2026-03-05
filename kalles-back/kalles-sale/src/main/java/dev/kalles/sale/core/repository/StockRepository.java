package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Stock s WHERE s.product.id = :productId")
    int sumQuantityByProductId(@Param("productId") UUID productId);

    Optional<Stock> findByProductIdAndLocationId(UUID productId, UUID locationId);

    List<Stock> findAllByProductIdOrderByQuantityDesc(UUID productId);

    List<Stock> findAllByLocationId(UUID locationId);
}
