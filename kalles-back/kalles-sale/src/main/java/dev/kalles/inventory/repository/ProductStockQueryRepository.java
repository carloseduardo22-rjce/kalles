package dev.kalles.inventory.repository;

import dev.kalles.inventory.dto.ProductStockSummary;
import dev.kalles.inventory.entity.Stock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductStockQueryRepository extends Repository<Stock, UUID> {

    @Query("""
        SELECT new dev.kalles.inventory.dto.ProductStockSummary(
            s.product.id, COALESCE(SUM(s.quantity), 0)
        )
        FROM Stock s
        JOIN s.location l
        JOIN l.warehouse w
        WHERE w.companyId = :companyId
          AND s.product.id IN :productIds
        GROUP BY s.product.id
    """)
    List<ProductStockSummary> summarizeByCompany(@Param("companyId") UUID companyId, @Param("productIds") Collection<UUID> productIds);
}
