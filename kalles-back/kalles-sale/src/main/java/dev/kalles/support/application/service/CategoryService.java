package dev.kalles.support.application.service;

import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.support.application.exception.NotFoundException;
import dev.kalles.support.domain.Priority;
import dev.kalles.support.infrastructure.persistence.entity.CategoryEntity;
import dev.kalles.support.infrastructure.persistence.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryEntity> listAllActive() {
        return categoryRepository.findAllByTenantIdAndActiveTrueOrderByNameAscSubcategoryAsc(currentTenantId());
    }

    @Transactional(readOnly = true)
    public CategoryEntity findById(UUID id) {
        return categoryRepository.findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }

    @Transactional
    public CategoryEntity create(String name, String subcategory, Priority defaultPriority) {
        UUID tenantId = currentTenantId();
        categoryRepository.findByTenantIdAndNameAndSubcategory(tenantId, name, subcategory).ifPresent(existing -> {
            throw new IllegalArgumentException(
                    "A category with name '" + name + "' and subcategory '" + subcategory + "' already exists.");
        });
        CategoryEntity category = new CategoryEntity();
        category.setTenantId(tenantId);
        category.setName(name);
        category.setSubcategory(subcategory);
        category.setDefaultPriority(defaultPriority);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    @Transactional
    public CategoryEntity update(UUID id, String name, String subcategory, Priority defaultPriority) {
        CategoryEntity category = findById(id);
        UUID tenantId = currentTenantId();
        categoryRepository.findByTenantIdAndNameAndSubcategory(tenantId, name, subcategory).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Another category already uses name '" + name + "' and subcategory '" + subcategory + "'.");
            }
        });
        category.setName(name);
        category.setSubcategory(subcategory);
        category.setDefaultPriority(defaultPriority);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deactivate(UUID id) {
        CategoryEntity category = findById(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required for support categories");
        }
        return tenantId;
    }
}
