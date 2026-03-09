package dev.kalles.support.domain;

import lombok.Getter;

import java.time.Instant;

/**
 * Represents a single message or note in a Ticket's timeline.
 * Can be a customer message, an agent reply, or an internal note.
 */
@Getter
public class Interaction {

    private final String content;
    private final InteractionType type;
    private final Instant createdAt;

    public Interaction(String content, InteractionType type) {
        this.content = content;
        this.type = type;
        this.createdAt = Instant.now();
    }

    /** Reconstitutes an Interaction from persisted data (preserves the original timestamp). */
    public static Interaction reconstitute(String content, InteractionType type, Instant createdAt) {
        return new Interaction(content, type, createdAt);
    }

    private Interaction(String content, InteractionType type, Instant createdAt) {
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
    }
}
