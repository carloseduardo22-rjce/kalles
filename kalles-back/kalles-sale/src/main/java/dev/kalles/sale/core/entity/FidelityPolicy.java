package dev.kalles.sale.core.entity;

import dev.kalles.sale.core.enums.fidelity.FidelityDiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fidelity_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FidelityPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "objective_points", nullable = false)
    private Integer objectivePoints;

    @Column(name = "configured_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal configuredDiscount;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private FidelityDiscountType discountType = FidelityDiscountType.FIXED;

    @Column(name = "value_point", nullable = false)
    private Integer valuePoint;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();
}
