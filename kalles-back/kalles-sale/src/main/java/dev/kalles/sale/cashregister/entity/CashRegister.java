package dev.kalles.sale.cashregister.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cash_registers",
    uniqueConstraints = @UniqueConstraint(name = "uk_cash_register_code_company", columnNames = {"code", "company_id"})
)
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public CashRegister(String code, String description, UUID companyId) {
        this.code = Objects.requireNonNull(code, "Código do caixa obrigatório");
        this.description = Objects.requireNonNull(description, "Descrição obrigatória");
        this.companyId = companyId;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
