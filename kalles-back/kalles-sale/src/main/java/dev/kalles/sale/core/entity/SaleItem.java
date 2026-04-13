package dev.kalles.sale.core.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
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
    @Index(name = "idx_saleitem_sale_id", columnList = "sale_id"),
    @Index(name = "idx_saleitem_product_id", columnList = "product_id")
}, comment = "Itens adicionados a uma venda: referência ao produto, quantidade, preço unitário e desconto aplicado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale; 

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice; 

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    public SaleItem(Sale sale, Product product, int quantity, BigDecimal unitPrice) {
        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice; 
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void incrementQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("A quantidade adicionada deve ser positiva.");
        }
        this.quantity += amount;
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    public void applyDiscount(BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "O valor do desconto não pode ser negativo.");
        }

        BigDecimal itemTotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));

        if (discountAmount.compareTo(itemTotal) > 0) {
            throw new IllegalArgumentException(
                "O desconto não pode exceder o valor do produto. Valor do item: R$ " + itemTotal);
        }

        this.discount = discountAmount;
    }

    public BigDecimal getSubtotal() {
        return this.unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(this.discount);
    }
}
