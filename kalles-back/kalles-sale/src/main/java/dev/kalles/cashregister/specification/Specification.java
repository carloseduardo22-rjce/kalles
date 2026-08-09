package dev.kalles.cashregister.specification;

public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
}
