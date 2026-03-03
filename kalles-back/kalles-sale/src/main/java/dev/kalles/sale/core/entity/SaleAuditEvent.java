package dev.kalles.sale.core.entity;

import dev.kalles.sale.cashregister.entity.Operator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sale_audit_events",
        indexes = @Index(name = "idx_audit_sale_id", columnList = "sale_id"))
@Getter
@NoArgsConstructor
public class SaleAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private SaleAuditEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity_removed")
    private Integer quantityRemoved;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private Operator requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorized_by_id")
    private Operator authorizedBy;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    public static SaleAuditEvent forItemRemoval(
            Sale sale,
            Product product,
            int quantityRemoved,
            Operator requestedBy,
            Operator authorizedBy) {

        SaleAuditEvent event = new SaleAuditEvent();
        event.sale = sale;
        event.eventType = SaleAuditEventType.ITEM_REMOVED;
        event.product = product;
        event.quantityRemoved = quantityRemoved;
        event.requestedBy = requestedBy;
        event.authorizedBy = authorizedBy;
        event.occurredAt = LocalDateTime.now();
        return event;
    }

    public static SaleAuditEvent forCancellation(
            Sale sale,
            Operator requestedBy,
            Operator authorizedBy) {

        SaleAuditEvent event = new SaleAuditEvent();
        event.sale = sale;
        event.eventType = SaleAuditEventType.SALE_CANCELLED;
        event.requestedBy = requestedBy;
        event.authorizedBy = authorizedBy;
        event.occurredAt = LocalDateTime.now();
        return event;
    }
}
