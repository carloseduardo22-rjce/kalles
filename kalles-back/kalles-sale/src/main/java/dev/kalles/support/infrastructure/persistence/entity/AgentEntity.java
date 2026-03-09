package dev.kalles.support.infrastructure.persistence.entity;

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
        @Index(name = "idx_agents_employee_id", columnList = "employee_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
public class AgentEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false, unique = true, length = 100)
    private String employeeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}
