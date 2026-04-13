package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    List<Warehouse> findAllByActiveTrueOrderByNameAsc();

    List<Warehouse> findAllByCompanyIdAndActiveTrueOrderByNameAsc(UUID companyId);

    Optional<Warehouse> findByIdAndCompanyId(UUID id, UUID companyId);
}
