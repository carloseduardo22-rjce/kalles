package dev.kalles.cashregister.repository;

import dev.kalles.cashregister.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, UUID> {
    Optional<Operator> findByCode(String code);

    Optional<Operator> findByCodeAndCompanyId(String code, UUID companyId);

    Optional<Operator> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<Operator> findByAccountIdAndCompanyId(UUID accountId, UUID companyId);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    List<Operator> findAllByActiveTrueOrderByNameAsc();

    List<Operator> findAllByCompanyIdAndActiveTrueOrderByNameAsc(UUID companyId);
}
