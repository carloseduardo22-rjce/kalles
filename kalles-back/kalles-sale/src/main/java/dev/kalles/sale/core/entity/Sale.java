package dev.kalles.sale.core.entity;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.kalles.sale.core.state.OpenState;
import dev.kalles.sale.core.state.SaleState;
import dev.kalles.sale.core.state.SaleStateConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
    @Index(name = "idx_sale_session_token", columnList = "session_token"),
    @Index(name = "idx_sale_state", columnList = "state")
}, comment = "Entidade principal representando uma venda: sessao, estado, itens, pagamentos e totais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "session_token", nullable = false)
    private String sessionToken;

    @Convert(converter = SaleStateConverter.class)
    @Column(nullable = false)
    private SaleState state = new OpenState();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SaleItem> items = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new LinkedHashSet<>();

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "amount_due", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountDue = BigDecimal.ZERO;

    public void addItem(Product product) { state.addItem(this, product); }
    public void removeItem(Product product) { state.removeItem(this, product); }
    public void applyItemDiscount(UUID itemId, BigDecimal discountAmount) { state.applyItemDiscount(this, itemId, discountAmount); }
    public void startPayment() { state.startPayment(this); }
    public void finishPayment() { state.finishPayment(this); }
    public void cancel() { state.cancel(this); }
    public void hold() { state.hold(this); }
    public void resume() { state.resume(this); }
    public void completeSale() { state.completeSale(this); }

    public void doStartPayment() { this.amountDue = this.total; }

    public void doAddItem(Product product) {
        addOrIncrementItem(product);
        recalculateTotals();
    }

    public void doRemoveItem(Product product) {
        items.removeIf(item -> item.getProduct().getId().equals(product.getId()));
        recalculateTotals();
    }

    public void doDecrementItem(Product product) {
        items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresent(SaleItem::decrementQuantity);
        recalculateTotals();
    }

    public int getItemQuantity(Product product) {
        return items.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .mapToInt(SaleItem::getQuantity)
                .findFirst()
                .orElse(0);
    }

    public void doApplyItemDiscount(UUID itemId, BigDecimal discountAmount) {
        SaleItem item = items.stream()
                .filter(i -> Objects.equals(i.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item nao encontrado na venda com o id: " + itemId));
        item.applyDiscount(discountAmount);
        recalculateTotals();
    }

    public void addPayment(Payment payment) {
        payment.setSale(this);
        this.payments.add(payment);
        if (payment.isConfirmed()) {
            BigDecimal effectiveAmount = payment.getAmount().subtract(payment.getChangeAmount());
            this.amountDue = this.amountDue.subtract(effectiveAmount);
        }
        if (this.amountDue.compareTo(BigDecimal.ZERO) == 0) {
            finishPayment();
        }
    }

    public static Sale createForSession(String sessionToken) {
        Sale sale = new Sale();
        sale.setSessionToken(sessionToken);
        sale.setState(new OpenState());
        return sale;
    }

    public String getStateName() {
        return state != null ? state.getName() : OpenState.NAME;
    }

    private void addOrIncrementItem(Product product) {
        Optional<SaleItem> existing = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().incrementQuantity();
        } else {
            this.items.add(new SaleItem(this, product, 1));
        }
    }

    private void recalculateTotals() {
        this.subtotal = items.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = this.subtotal;
    }
}
