package dev.kalles.sale.cashregister.entity;

import dev.kalles.sale.core.enums.operator.PermissionLevel;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "operators")
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

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private PermissionLevel permissionLevel;

    public Operator(String name, String code) {
        this.name = Objects.requireNonNull(name, "Nome obrigatório");
        this.code = Objects.requireNonNull(code, "Código obrigatório");
    }
}
