package dev.kalles.sale.state;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Sale;

public interface SaleState {

    String getName();

    String getDescription();

    void addItem(Sale sale, Product product, BigDecimal unitPrice);

    void removeItem(Sale sale, Product product);

    void applyItemDiscount(Sale sale, UUID itemId, BigDecimal discountAmount);

    void startPayment(Sale sale);

    void finishPayment(Sale sale);

    void cancel(Sale sale);

    void hold(Sale sale);

    void resume(Sale sale);

    void completeSale(Sale sale);

    void applyFidelityDiscount(Sale sale, BigDecimal discount);
}
