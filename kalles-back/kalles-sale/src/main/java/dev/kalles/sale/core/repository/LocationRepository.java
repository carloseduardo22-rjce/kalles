package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findAllByWarehouseIdOrderByCodeAsc(UUID warehouseId);
}
