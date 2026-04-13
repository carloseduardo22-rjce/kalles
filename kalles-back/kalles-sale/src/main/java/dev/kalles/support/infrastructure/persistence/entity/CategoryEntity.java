package dev.kalles.support.infrastructure.persistence.entity;

import dev.kalles.support.domain.Priority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "categories",
    schema = "support",
    comment = "Ticket categories that define default priority",
    indexes = {
        @Index(name = "idx_categories_tenant_name_subcategory", columnList = "tenant_id, name, subcategory", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String subcategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_priority", nullable = false, length = 20)
    private Priority defaultPriority;

    @Column(nullable = false)
    private boolean active = true;
}
