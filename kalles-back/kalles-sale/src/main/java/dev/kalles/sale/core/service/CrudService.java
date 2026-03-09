package dev.kalles.sale.core.service;

import java.util.List;
import java.util.Optional;

public interface CrudService<T, ID> {

    T create(T entity);

    List<T> findAll();

    Optional<T> findById(ID id);

    T update(T entity);

    void delete(ID id);
}
