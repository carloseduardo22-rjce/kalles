package dev.kalles.cashregister.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cash_register_closing_payment_totals")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CashRegisterClosingPaymentTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "closing_id", nullable = false)
    private CashRegisterClosing closing;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public CashRegisterClosingPaymentTotal(CashRegisterClosing closing, String paymentMethod, BigDecimal amount) {
        this.closing = Objects.requireNonNull(closing, "Fechamento obrigatorio");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Metodo de pagamento obrigatorio");
        this.amount = Objects.requireNonNull(amount, "Valor obrigatorio");
    }
}
