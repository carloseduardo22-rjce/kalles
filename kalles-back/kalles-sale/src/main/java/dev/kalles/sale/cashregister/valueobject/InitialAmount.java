package dev.kalles.sale.cashregister.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class InitialAmount {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    public InitialAmount(BigDecimal value) {
        validate(value);
        this.value = value;
    }

    private void validate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Valor inicial não pode ser nulo");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor inicial não pode ser negativo");
        }
    }
}
