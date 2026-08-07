package dev.kalles.inventory.repository;

import dev.kalles.inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findAllByWarehouseIdOrderByCodeAsc(UUID warehouseId);

    @Query("SELECT l FROM Location l WHERE l.id = :id AND l.warehouse.companyId = :companyId")
    Optional<Location> findByIdAndCompanyId(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
