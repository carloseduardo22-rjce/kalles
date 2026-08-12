package dev.kalles.inventory.service;

import dev.kalles.inventory.dto.LocationRequest;
import dev.kalles.inventory.dto.LocationResponse;
import dev.kalles.inventory.entity.Location;
import dev.kalles.inventory.entity.Warehouse;
import dev.kalles.inventory.repository.LocationRepository;
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
public class LocationService {

    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    public LocationResponse create(UUID warehouseId, LocationRequest request) {
        Warehouse warehouse = warehouseRepository.findByIdAndCompanyId(warehouseId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + warehouseId));
        Location location = new Location();
        location.setWarehouse(warehouse);
        location.setCode(request.code());
        location.setDescription(request.description());
        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> listByWarehouse(UUID warehouseId) {
        // Validate warehouse belongs to current company
        warehouseRepository.findByIdAndCompanyId(warehouseId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + warehouseId));
        return locationRepository.findAllByWarehouseIdOrderByCodeAsc(warehouseId)
                .stream()
                .map(LocationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse findById(UUID locationId) {
        return locationRepository.findByIdAndCompanyId(locationId, CompanyContextHolder.requireCompanyId())
                .map(LocationResponse::from)
                .orElseThrow(() -> new NotFoundException("Localização não encontrada: " + locationId));
    }

    @Transactional
    public LocationResponse update(UUID locationId, LocationRequest request) {
        Location location = locationRepository.findByIdAndCompanyId(locationId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Localização não encontrada: " + locationId));
        location.setCode(request.code());
        location.setDescription(request.description());
        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional
    public void delete(UUID locationId) {
        Location location = locationRepository.findByIdAndCompanyId(locationId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Localização não encontrada: " + locationId));
        locationRepository.delete(location);
    }
}
