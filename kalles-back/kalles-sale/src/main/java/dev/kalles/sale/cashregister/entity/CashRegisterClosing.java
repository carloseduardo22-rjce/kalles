package dev.kalles.sale.cashregister.entity;

import dev.kalles.sale.cashregister.dto.SessionSummaryResponse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cash_register_closings")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CashRegisterClosing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private CashRegisterSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorized_by_operator_id", nullable = false)
    private Operator authorizedByOperator;

    @Column(name = "completed_sales_count", nullable = false)
    private int completedSalesCount;

    @Column(name = "canceled_sales_count", nullable = false)
    private int canceledSalesCount;

    @Column(name = "total_sold_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSoldAmount;

    @Column(name = "cash_sales_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashSalesAmount;

    @Column(name = "expected_cash_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedCashAmount;

    @Column(name = "counted_cash_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal countedCashAmount;

    @Column(name = "cash_difference_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashDifferenceAmount;

    @OneToMany(mappedBy = "closing", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CashRegisterClosingPaymentTotal> paymentTotals = new LinkedHashSet<>();

    public static CashRegisterClosing create(
            CashRegisterSession session,
            Operator authorizedByOperator,
            SessionSummaryResponse summary,
            BigDecimal countedCashAmount
    ) {
        return new CashRegisterClosing(session, authorizedByOperator, summary, countedCashAmount);
    }

    private CashRegisterClosing(
            CashRegisterSession session,
            Operator authorizedByOperator,
            SessionSummaryResponse summary,
            BigDecimal countedCashAmount
    ) {
        this.session = Objects.requireNonNull(session, "Sessao obrigatoria");
        this.authorizedByOperator = Objects.requireNonNull(authorizedByOperator, "Operador autorizador obrigatorio");
        this.completedSalesCount = summary.vendasConcluidas();
        this.canceledSalesCount = summary.vendasCanceladas();
        this.totalSoldAmount = defaultValue(summary.totalVendido());
        this.cashSalesAmount = defaultValue(summary.totalEmDinheiro());
        this.expectedCashAmount = defaultValue(summary.saldoEsperadoEmCaixa());
        this.countedCashAmount = requireNonNegative(countedCashAmount);
        this.cashDifferenceAmount = this.countedCashAmount.subtract(this.expectedCashAmount);

        for (Map.Entry<String, BigDecimal> entry : summary.totalPorMetodoPagamento().entrySet()) {
            this.paymentTotals.add(new CashRegisterClosingPaymentTotal(this, entry.getKey(), defaultValue(entry.getValue())));
        }
    }

    public SessionSummaryResponse toSummaryResponse() {
        Map<String, BigDecimal> totalPorMetodo = paymentTotals.stream().collect(
                java.util.stream.Collectors.toMap(
                        CashRegisterClosingPaymentTotal::getPaymentMethod,
                        CashRegisterClosingPaymentTotal::getAmount,
                        BigDecimal::add,
                        LinkedHashMap::new
                )
        );

        return new SessionSummaryResponse(
                completedSalesCount,
                canceledSalesCount,
                totalSoldAmount,
                totalPorMetodo,
                cashSalesAmount,
                expectedCashAmount,
                countedCashAmount,
                cashDifferenceAmount
        );
    }

    private static BigDecimal requireNonNegative(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Valor contado em caixa nao pode ser nulo");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor contado em caixa nao pode ser negativo");
        }
        return value;
    }

    private static BigDecimal defaultValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
