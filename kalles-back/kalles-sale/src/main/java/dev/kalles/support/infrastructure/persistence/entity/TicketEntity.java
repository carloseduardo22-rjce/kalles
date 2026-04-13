package dev.kalles.support.infrastructure.persistence.entity;

import dev.kalles.support.domain.Priority;
import dev.kalles.support.domain.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "tickets",
    schema = "support",
    comment = "Support tickets — core aggregate of the helpdesk domain",
    indexes = {
        @Index(name = "idx_tickets_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_tickets_company_id", columnList = "company_id"),
        @Index(name = "idx_tickets_status", columnList = "status"),
        @Index(name = "idx_tickets_user_id", columnList = "user_id"),
        @Index(name = "idx_tickets_agent_id", columnList = "agent_id"),
        @Index(name = "idx_tickets_category_id", columnList = "category_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class TicketEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private AgentEntity agent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "sla_active", nullable = false)
    private boolean slaActive;

    @Column(name = "sla_started_at")
    private Instant slaStartedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<InteractionEntity> interactions = new ArrayList<>();
}
