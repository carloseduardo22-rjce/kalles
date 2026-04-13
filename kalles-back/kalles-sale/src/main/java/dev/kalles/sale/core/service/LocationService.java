package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.LocationRequest;
import dev.kalles.sale.core.dto.LocationResponse;
import dev.kalles.sale.core.entity.Location;
import dev.kalles.sale.core.entity.Warehouse;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.LocationRepository;
import dev.kalles.sale.core.repository.WarehouseRepository;
import dev.kalles.sale.security.context.CompanyContextHolder;
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
        Warehouse warehouse = warehouseRepository.findByIdAndCompanyId(warehouseId, getCompanyId())
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
        warehouseRepository.findByIdAndCompanyId(warehouseId, getCompanyId())
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado: " + warehouseId));
        return locationRepository.findAllByWarehouseIdOrderByCodeAsc(warehouseId)
                .stream()
                .map(LocationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse findById(UUID locationId) {
        return locationRepository.findByIdAndCompanyId(locationId, getCompanyId())
                .map(LocationResponse::from)
                .orElseThrow(() -> new NotFoundException("Localização não encontrada: " + locationId));
    }

    @Transactional
    public LocationResponse update(UUID locationId, LocationRequest request) {
        Location location = locationRepository.findByIdAndCompanyId(locationId, getCompanyId())
                .orElseThrow(() -> new NotFoundException("Localização não encontrada: " + locationId));
        location.setCode(request.code());
        location.setDescription(request.description());
        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional
    public void delete(UUID locationId) {
        Location location = locationRepository.findByIdAndCompanyId(locationId, getCompanyId())
                .orElseThrow(() -> new NotFoundException("Localização não encontrada: " + locationId));
        locationRepository.delete(location);
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
    }
}
