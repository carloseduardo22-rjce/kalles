package dev.kalles.support.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "agents",
    schema = "support",
    comment = "Support agents who handle tickets",
    indexes = {
        @Index(name = "idx_agents_tenant_employee_id", columnList = "tenant_id, employee_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
public class AgentEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id", nullable = false, length = 100)
    private String employeeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}
