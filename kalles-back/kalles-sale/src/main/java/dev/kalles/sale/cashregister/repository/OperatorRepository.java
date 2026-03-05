package dev.kalles.sale.cashregister.repository;

import dev.kalles.sale.cashregister.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, UUID> {
    Optional<Operator> findByCode(String code);
    java.util.List<Operator> findAllByActiveTrueOrderByNameAsc();
}
