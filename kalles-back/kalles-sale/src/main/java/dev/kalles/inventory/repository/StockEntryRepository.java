package dev.kalles.inventory.repository;

import dev.kalles.core.dto.SupplierExpenseProductSummary;
import dev.kalles.inventory.entity.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface StockEntryRepository extends JpaRepository<StockEntry, UUID> {

    @Query("""
        SELECT COALESCE(SUM(se.totalCost), 0)
        FROM StockEntry se
        WHERE se.companyId = :companyId
          AND se.createdAt >= :start
          AND se.createdAt < :end
        """)
    BigDecimal sumTotalCostBetween(
        @Param("companyId") UUID companyId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT new dev.kalles.core.dto.SupplierExpenseProductSummary(
            se.product.id,
            se.product.name,
            se.product.internalCode,
            SUM(se.quantityAdded),
            COALESCE(SUM(se.totalCost), 0)
        )
        FROM StockEntry se
        WHERE se.companyId = :companyId
          AND se.createdAt >= :start
          AND se.createdAt < :end
        GROUP BY se.product.id, se.product.name, se.product.internalCode
        ORDER BY COALESCE(SUM(se.totalCost), 0) DESC, se.product.name ASC
        """)
    List<SupplierExpenseProductSummary> summarizeByProductBetween(
        @Param("companyId") UUID companyId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
