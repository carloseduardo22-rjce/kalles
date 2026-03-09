package dev.kalles.support.infrastructure.persistence.repository;

import dev.kalles.support.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByNameAndSubcategory(String name, String subcategory);

    List<CategoryEntity> findAllByActiveTrueOrderByNameAscSubcategoryAsc();

    List<CategoryEntity> findAllByOrderByNameAscSubcategoryAsc();
}
