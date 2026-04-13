package dev.kalles.support.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "users",
    schema = "support",
    comment = "Customers who open support tickets",
    indexes = {
        @Index(name = "idx_users_tenant_email", columnList = "tenant_id, email", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String name;
}
