package dev.kalles.sale.cashregister.entity;

import dev.kalles.sale.cashregister.valueobject.InitialAmount;
import dev.kalles.sale.cashregister.valueobject.SessionPeriod;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cash_register_sessions")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CashRegisterSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "initial_amount", nullable = false, precision = 19, scale = 2))
    private InitialAmount initialAmount;

    @Embedded
    private SessionPeriod sessionPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    public static CashRegisterSession open(
            CashRegister cashRegister,
            Operator operator,
            BigDecimal initialAmountValue
    ) {
        return new CashRegisterSession(
            cashRegister,
            operator,
            new InitialAmount(initialAmountValue),
            new SessionPeriod(LocalDateTime.now())
        );
    }

    private CashRegisterSession(
            CashRegister cashRegister,
            Operator operator,
            InitialAmount initialAmount,
            SessionPeriod sessionPeriod
    ) {
        this.cashRegister = Objects.requireNonNull(cashRegister, "Caixa obrigatório");
        this.operator = Objects.requireNonNull(operator, "Operador obrigatório");
        this.initialAmount = Objects.requireNonNull(initialAmount, "Valor inicial obrigatório");
        this.sessionPeriod = Objects.requireNonNull(sessionPeriod, "Período obrigatório");
        this.status = SessionStatus.OPEN;
    }

    public boolean isOpen() {
        return status == SessionStatus.OPEN;
    }

    public void close() {
        if (!isOpen()) {
            throw new IllegalStateException("Sessão já está fechada");
        }
        sessionPeriod.close(LocalDateTime.now());
        this.status = SessionStatus.CLOSED;
    }

    public BigDecimal getInitialAmountValue() {
        return initialAmount.getValue();
    }

    public LocalDateTime getOpenedAt() {
        return sessionPeriod.getOpenedAt();
    }

    public LocalDateTime getClosedAt() {
        return sessionPeriod.getClosedAt();
    }
}
