package dev.kalles.inventory.service;

import dev.kalles.inventory.dto.WarehouseRequest;
import dev.kalles.inventory.dto.WarehouseResponse;
import dev.kalles.inventory.entity.Warehouse;
import dev.kalles.inventory.repository.WarehouseRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.NotFoundException;
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
        warehouse.setCompanyId(CompanyContextHolder.requireCompanyId());
        warehouse.setActive(true);
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> listActiveWarehouses() {
        return warehouseRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(CompanyContextHolder.requireCompanyId())
                .stream()
                .map(WarehouseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse findWarehouseById(UUID id) {
        return warehouseRepository.findByIdAndCompanyId(id, CompanyContextHolder.requireCompanyId())
                .map(WarehouseResponse::from)
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + id));
    }

    @Transactional
    public WarehouseResponse updateWarehouse(UUID id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findByIdAndCompanyId(id, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + id));
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void deactivateWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findByIdAndCompanyId(id, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + id));
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }
}
