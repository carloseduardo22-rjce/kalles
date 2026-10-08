package dev.kalles.sale.state;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.exception.SaleStateTransitionException;

public abstract class AbstractSaleState implements SaleState {

    @Override
    public void addItem(Sale sale, Product product, BigDecimal unitPrice) {
        throw new SaleStateTransitionException("adicionar itens", getDescription());
    }

    @Override
    public void removeItem(Sale sale, Product product) {
        throw new SaleStateTransitionException("remover itens", getDescription());
    }

    @Override
    public void applyItemDiscount(Sale sale, UUID itemId, BigDecimal discountAmount) {
        throw new SaleStateTransitionException("aplicar desconto", getDescription());
    }

    @Override
    public void startPayment(Sale sale) {
        throw new SaleStateTransitionException("iniciar pagamento", getDescription());
    }

    @Override
    public void finishPayment(Sale sale) {
        throw new SaleStateTransitionException("finalizar pagamento", getDescription());
    }

    @Override
    public void cancel(Sale sale) {
        throw new SaleStateTransitionException("cancelar", getDescription());
    }

    @Override
    public void completeSale(Sale sale) {
        throw new SaleStateTransitionException("finalizar a venda", getDescription());
    }

    @Override
    public void applyFidelityDiscount(Sale sale, BigDecimal discount) {
        throw new SaleStateTransitionException("aplicar desconto de fidelidade", getDescription());
    }
}
