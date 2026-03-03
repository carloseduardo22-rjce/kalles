package dev.kalles.sale.cashregister.specification;

public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
}
