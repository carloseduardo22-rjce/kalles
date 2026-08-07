package dev.kalles.cashregister.entity;

import dev.kalles.core.enums.operator.PermissionLevel;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "operators",
    uniqueConstraints = @UniqueConstraint(name = "uk_operator_code_company", columnNames = {"code", "company_id"})
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private PermissionLevel permissionLevel;

    @Column(nullable = false)
    private boolean active = true;

    public Operator(String name, String code) {
        this.name = Objects.requireNonNull(name, "Nome obrigatório");
        this.code = Objects.requireNonNull(code, "Código obrigatório");
    }
}
