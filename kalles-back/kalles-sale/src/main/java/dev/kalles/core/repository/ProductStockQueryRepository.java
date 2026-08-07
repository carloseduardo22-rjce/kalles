package dev.kalles.core.repository;

import dev.kalles.core.dto.ProductLocationStockView;
import dev.kalles.core.dto.ProductStockSummary;
import dev.kalles.core.entity.Stock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductStockQueryRepository extends Repository<Stock, UUID> {

    @Query("""
        SELECT new dev.kalles.core.dto.ProductStockSummary(
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

    @Query("""
        SELECT new dev.kalles.core.dto.ProductLocationStockView(
            w.id, w.name, l.id, l.code, SUM(s.quantity)
        )
        FROM Stock s
        JOIN s.location l
        JOIN l.warehouse w
        WHERE s.product.id = :productId
          AND w.companyId = :companyId
        GROUP BY w.id, w.name, l.id, l.code
        ORDER BY w.name ASC, l.code ASC
    """)
    List<ProductLocationStockView> breakdown(@Param("companyId") UUID companyId, @Param("productId") UUID productId);
}
