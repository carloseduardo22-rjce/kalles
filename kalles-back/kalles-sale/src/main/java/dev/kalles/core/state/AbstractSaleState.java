package dev.kalles.core.state;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.core.entity.Product;
import dev.kalles.core.entity.Sale;

public abstract class AbstractSaleState implements SaleState {

    @Override
    public void addItem(Sale sale, Product product, BigDecimal unitPrice) {
        throw new IllegalStateException(
                "Não é possível adicionar itens no estado: " + getDescription());
    }

    @Override
    public void removeItem(Sale sale, Product product) {
        throw new IllegalStateException(
                "Não é possível remover itens no estado: " + getDescription());
    }

    @Override
    public void applyItemDiscount(Sale sale, UUID itemId, BigDecimal discountAmount) {
        throw new IllegalStateException(
                "Não é possível aplicar desconto no estado: " + getDescription());
    }

    @Override
    public void startPayment(Sale sale) {
        throw new IllegalStateException(
                "Não é possível iniciar pagamento no estado: " + getDescription());
    }

    @Override
    public void finishPayment(Sale sale) {
        throw new IllegalStateException(
                "Não é possível finalizar pagamento no estado: " + getDescription());
    }

    @Override
    public void cancel(Sale sale) {
        throw new IllegalStateException(
                "Não é possível cancelar no estado: " + getDescription());
    }

    @Override
    public void hold(Sale sale) {
        throw new IllegalStateException(
                "Não é possível colocar em espera no estado: " + getDescription());
    }

    @Override
    public void resume(Sale sale) {
        throw new IllegalStateException(
                "Não é possível retomar no estado: " + getDescription());
    }

    @Override
    public void completeSale(Sale sale) {
        throw new IllegalStateException(
                "Não é possível finalizar a venda no estado: " + getDescription());
    }

    @Override
    public void applyFidelityDiscount(Sale sale, BigDecimal discount) {
        throw new IllegalStateException(
                "Não é possível aplicar desconto de fidelidade no estado: " + getDescription());
    }
}
