package dev.kalles.core.repository;

import dev.kalles.core.dto.CompanyProductListItem;
import dev.kalles.core.entity.CompanyProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CompanyProductReadRepository extends Repository<CompanyProduct, UUID> {

    @Query("""
        SELECT new dev.kalles.core.dto.CompanyProductListItem(
            p.id, p.name, p.internalCode, p.barcode,
            cp.price, cp.costPrice, p.description, cp.active
        )
        FROM CompanyProduct cp
        JOIN cp.product p
        WHERE cp.companyId = :companyId
          AND (:includeInactive = true OR cp.active = true)
        ORDER BY p.name ASC
    """)
    List<CompanyProductListItem> listCatalog(@Param("companyId") UUID companyId, @Param("includeInactive") boolean includeInactive);

    @Query(
            value = """
        SELECT new dev.kalles.core.dto.CompanyProductListItem(
            p.id, p.name, p.internalCode, p.barcode,
            cp.price, cp.costPrice, p.description, cp.active
        )
        FROM CompanyProduct cp
        JOIN cp.product p
        WHERE cp.companyId = :companyId
          AND (:includeInactive = true OR cp.active = true)
        ORDER BY p.name ASC
    """,
            countQuery = """
        SELECT COUNT(cp)
        FROM CompanyProduct cp
        JOIN cp.product p
        WHERE cp.companyId = :companyId
          AND (:includeInactive = true OR cp.active = true)
    """
    )
    Page<CompanyProductListItem> listCatalogPage(
            @Param("companyId") UUID companyId,
            @Param("includeInactive") boolean includeInactive,
            Pageable pageable
    );

    @Query("""
        SELECT new dev.kalles.core.dto.CompanyProductListItem(
            p.id, p.name, p.internalCode, p.barcode,
            cp.price, cp.costPrice, p.description, cp.active
        )
        FROM CompanyProduct cp
        JOIN cp.product p
        WHERE cp.companyId = :companyId
          AND cp.active = true
          AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.internalCode) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY p.name ASC
    """)
    List<CompanyProductListItem> searchActiveCatalog(@Param("companyId") UUID companyId, @Param("q") String q);
}
