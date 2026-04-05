package dev.kalles.support.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerMessageRequest(
        @NotBlank
        String content
) {
}
