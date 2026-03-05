package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.WarehouseRequest;
import dev.kalles.sale.core.dto.WarehouseResponse;
import dev.kalles.sale.core.entity.Warehouse;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        warehouse.setActive(true);
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> listActiveWarehouses() {
        return warehouseRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(WarehouseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse findWarehouseById(UUID id) {
        return warehouseRepository.findById(id)
                .map(WarehouseResponse::from)
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + id));
    }

    @Transactional
    public WarehouseResponse updateWarehouse(UUID id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + id));
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void deactivateWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + id));
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }
}
