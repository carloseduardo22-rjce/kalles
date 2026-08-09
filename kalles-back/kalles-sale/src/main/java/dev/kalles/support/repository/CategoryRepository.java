package dev.kalles.support.repository;

import dev.kalles.support.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByTenantIdAndNameAndSubcategory(UUID tenantId, String name, String subcategory);

    List<CategoryEntity> findAllByTenantIdAndActiveTrueOrderByNameAscSubcategoryAsc(UUID tenantId);

    List<CategoryEntity> findAllByTenantIdOrderByNameAscSubcategoryAsc(UUID tenantId);

    Optional<CategoryEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
