package dev.kalles.core.entity;

import dev.kalles.client.entity.Client;
import dev.kalles.core.enums.fidelity.FidelityDiscountType;
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
        // Deduz o objetivo em vez de zerar: pontos excedentes são preservados.
        // O laço permite converter múltiplas recompensas acumuladas de uma vez.
        while (this.points >= policy.getObjectivePoints()) {
            BigDecimal reward = this.policy.getConfiguredDiscount();
            if (this.policy.getDiscountType() == FidelityDiscountType.PERCENTAGE) {
                this.availableDiscount = this.availableDiscount.add(reward).min(new BigDecimal("100"));
            } else {
                this.availableDiscount = this.availableDiscount.add(reward);
            }
            this.points -= policy.getObjectivePoints();
        }
    }

    /**
     * Calcula o desconto aplicável sobre o total informado SEM consumir o saldo.
     * O consumo ocorre apenas na conclusão da venda (consumeAppliedDiscount);
     * assim, venda abandonada ou cancelada não queima o benefício do cliente.
     */
    public BigDecimal previewDiscount(BigDecimal saleTotal) {
        if (this.policy.getDiscountType() == FidelityDiscountType.PERCENTAGE) {
            return saleTotal
                    .multiply(this.availableDiscount)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                    .min(saleTotal);
        }
        return this.availableDiscount.min(saleTotal);
    }

    /**
     * Consome o saldo referente ao desconto efetivamente aplicado numa venda concluída.
     * Percentual é benefício de uso único: zera ao ser utilizado.
     */
    public void consumeAppliedDiscount(BigDecimal appliedAmount) {
        if (this.policy.getDiscountType() == FidelityDiscountType.PERCENTAGE) {
            this.availableDiscount = BigDecimal.ZERO;
            return;
        }
        this.availableDiscount = this.availableDiscount.subtract(appliedAmount).max(BigDecimal.ZERO);
    }

    public boolean isActuallyExpired() {
        return LocalDate.now().isAfter(createdAt.plusMonths(3));
    }
}
