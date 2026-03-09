package dev.kalles.support.domain;

import java.time.Instant;

/**
 * Immutable Value Object representing the Service Level Agreement of a Ticket.
 * To pause or restart the SLA, a new instance must be created.
 */
public final class Sla {

    private final boolean active;
    private final Instant startedAt;

    private Sla(boolean active, Instant startedAt) {
        this.active = active;
        this.startedAt = startedAt;
    }

    /** Creates an active SLA, starting the counter from now. */
    public static Sla start() {
        return new Sla(true, Instant.now());
    }

    /** Creates an inactive SLA (for tickets reconstituted in a terminal state). */
    public static Sla inactive() {
        return new Sla(false, null);
    }

    /** Reconstitutes an SLA from persisted data. */
    public static Sla reconstitute(boolean active, Instant startedAt) {
        return new Sla(active, startedAt);
    }

    public boolean isActive() {
        return active;
    }

    public Instant getStartedAt() {
        return startedAt;
    }
}
