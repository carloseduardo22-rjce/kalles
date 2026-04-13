package dev.kalles.sale.cashregister.repository;

import dev.kalles.sale.cashregister.entity.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, UUID> {
    Optional<CashRegister> findByCode(String code);

    Optional<CashRegister> findByCodeAndCompanyId(String code, UUID companyId);

    Optional<CashRegister> findByIdAndCompanyId(UUID id, UUID companyId);

    List<CashRegister> findAllByActiveTrueOrderByCodeAsc();

    List<CashRegister> findAllByCompanyIdAndActiveTrueOrderByCodeAsc(UUID companyId);
}
