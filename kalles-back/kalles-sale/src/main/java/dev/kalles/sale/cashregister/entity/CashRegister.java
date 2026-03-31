package dev.kalles.sale.cashregister.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cash_registers")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public CashRegister(String code, String description) {
        this.code = Objects.requireNonNull(code, "Código do caixa obrigatório");
        this.description = Objects.requireNonNull(description, "Descrição obrigatória");
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
