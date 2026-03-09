package dev.kalles.support.infrastructure.persistence.entity;

import dev.kalles.support.domain.InteractionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "interactions",
    schema = "support",
    comment = "Timeline of messages and notes within a ticket",
    indexes = {
        @Index(name = "idx_interactions_ticket_id", columnList = "ticket_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class InteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InteractionType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
