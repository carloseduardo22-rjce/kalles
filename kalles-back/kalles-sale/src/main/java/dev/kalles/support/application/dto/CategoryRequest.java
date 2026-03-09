package dev.kalles.support.application.dto;

import dev.kalles.support.domain.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

    @Schema(description = "Main category name", example = "System")
    @NotBlank @Size(max = 150)
    String name,

    @Schema(description = "Subcategory name", example = "Bug")
    @NotBlank @Size(max = 150)
    String subcategory,

    @Schema(description = "Default priority applied to tickets in this category",
            allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    @NotNull
    Priority defaultPriority
) {}
