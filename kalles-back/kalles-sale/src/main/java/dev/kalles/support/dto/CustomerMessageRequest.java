package dev.kalles.support.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerMessageRequest(
        @NotBlank
        String content
) {
}
