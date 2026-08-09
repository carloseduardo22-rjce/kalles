package dev.kalles.support.dto;

import dev.kalles.support.domain.Interaction;

import java.time.Instant;

public record InteractionResponse(
    String id,
    String content,
    String type,
    Instant createdAt
) {
    public static InteractionResponse from(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(),
                interaction.getContent(),
                interaction.getType().name(),
                interaction.getCreatedAt()
        );
    }
}
