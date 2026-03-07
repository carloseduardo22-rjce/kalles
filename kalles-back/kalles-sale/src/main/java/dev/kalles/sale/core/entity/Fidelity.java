package dev.kalles.sale.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fidelity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fidelity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(name = "available_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal availableDiscount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(nullable = false)
    private boolean expired = false;

    @OneToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private FidelityPolicy policy;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    public int calculatePoints(BigDecimal cash) {
        int earned = cash.divide(BigDecimal.valueOf(policy.getValuePoint()), 0, RoundingMode.DOWN).intValue();
        this.points += earned;
        return earned;
    }

    public void checkObjectivePoints() {
        if (this.points >= policy.getObjectivePoints()) {
            this.availableDiscount = this.policy.getConfiguredDiscount();
            this.points = 0;
        }
    }

    public BigDecimal consumeDiscount(BigDecimal saleTotal) {
        BigDecimal applied = this.availableDiscount.min(saleTotal);
        this.availableDiscount = this.availableDiscount.subtract(applied);
        return applied;
    }

    public void rollbackPoints(int pointsToRollback) {
        this.points = Math.max(0, this.points - pointsToRollback);
    }

    public void rollbackDiscount(BigDecimal amount) {
        this.availableDiscount = this.availableDiscount.add(amount);
    }

    public boolean isActuallyExpired() {
        return LocalDate.now().isAfter(createdAt.plusMonths(3));
    }
}
