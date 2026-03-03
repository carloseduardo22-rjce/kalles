package dev.kalles.sale.cashregister.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class SessionPeriod {

    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    public SessionPeriod(LocalDateTime openedAt) {
        Objects.requireNonNull(openedAt, "Data de abertura obrigatória");
        this.openedAt = openedAt;
        this.closedAt = null;
    }

    public boolean isOpen() {
        return closedAt == null;
    }

    public void close(LocalDateTime closedAt) {
        if (!isOpen()) {
            throw new IllegalStateException("Sessão já está fechada");
        }
        if (closedAt.isBefore(openedAt)) {
            throw new IllegalArgumentException("Data de fechamento anterior à abertura");
        }
        this.closedAt = closedAt;
    }
}
