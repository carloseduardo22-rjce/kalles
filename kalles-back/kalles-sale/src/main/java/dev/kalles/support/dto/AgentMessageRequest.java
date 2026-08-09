package dev.kalles.support.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentMessageRequest(
        @NotBlank
        String content,
        boolean markAsResolved
) {
}
