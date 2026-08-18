package dev.kalles.sale.entity;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.shared.entity.BaseAuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
    @Index(name = "idx_payment_sale_id", columnList = "sale_id")
}, comment = "Pagamentos realizados em uma venda: método, valor, troco, identificação da transação e status de confirmação"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "change_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal changeAmount = BigDecimal.ZERO;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(nullable = false)
    private boolean confirmed;
    

    public Payment(Sale sale, PaymentMethod method, BigDecimal amount, BigDecimal changeAmount, String transactionId, boolean confirmed) {
        this.sale = sale;
        this.method = method;
        this.amount = amount;
        this.changeAmount = changeAmount != null ? changeAmount : BigDecimal.ZERO;
        this.transactionId = transactionId;
        this.confirmed = confirmed;
        // createdAt will be populated by JPA Auditing
    }
}
