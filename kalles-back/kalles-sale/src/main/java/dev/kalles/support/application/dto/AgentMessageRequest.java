package dev.kalles.support.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentMessageRequest(
        @NotBlank
        String content,
        boolean markAsResolved
) {
}
