package dev.kalles.support.application.dto;

import dev.kalles.support.infrastructure.persistence.entity.CategoryEntity;

import java.util.UUID;

public record CategoryResponse(
    UUID id,
    String name,
    String subcategory,
    String defaultPriority,
    boolean active
) {
    public static CategoryResponse from(CategoryEntity category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSubcategory(),
                category.getDefaultPriority().name(),
                category.isActive()
        );
    }
}
