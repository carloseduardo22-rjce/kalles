package dev.kalles.core.service;

import dev.kalles.core.dto.LocationRequest;
import dev.kalles.core.dto.LocationResponse;
import dev.kalles.core.entity.Location;
import dev.kalles.core.entity.Warehouse;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.LocationRepository;
import dev.kalles.core.repository.WarehouseRepository;
import dev.kalles.security.context.CompanyContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService - Serviço de Localizações")
class LocationServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    private Warehouse buildWarehouse(UUID id) {
        return new Warehouse(id, "Dep A", COMPANY_ID, "End. A", true);
    }

    private Location buildLocation(UUID locationId, Warehouse warehouse, String code) {
        return new Location(locationId, warehouse, code, "Descrição de " + code);
    }

    @Test
    @DisplayName("Deve criar localização com sucesso")
    void shouldCreateLocationSuccessfully() {
        UUID warehouseId = UUID.randomUUID();
        Warehouse warehouse = buildWarehouse(warehouseId);
        LocationRequest request = new LocationRequest("EST-01", "Prateleira superior");
        Location saved = buildLocation(UUID.randomUUID(), warehouse, "EST-01");

        when(warehouseRepository.findByIdAndCompanyId(warehouseId, COMPANY_ID)).thenReturn(Optional.of(warehouse));
        when(locationRepository.save(any(Location.class))).thenReturn(saved);

        LocationResponse response = locationService.create(warehouseId, request);

        assertNotNull(response);
        assertEquals("EST-01", response.code());
        assertEquals(warehouseId, response.warehouseId());
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar localização em depósito inexistente")
    void shouldThrowNotFoundWhenCreatingLocationForNonExistentWarehouse() {
        UUID warehouseId = UUID.randomUUID();
        when(warehouseRepository.findByIdAndCompanyId(warehouseId, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> locationService.create(warehouseId, new LocationRequest("X", null)));
        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar localizações por depósito")
    void shouldListLocationsByWarehouse() {
        UUID warehouseId = UUID.randomUUID();
        Warehouse warehouse = buildWarehouse(warehouseId);
        List<Location> locations = List.of(
                buildLocation(UUID.randomUUID(), warehouse, "A1"),
                buildLocation(UUID.randomUUID(), warehouse, "A2")
        );

        when(warehouseRepository.findByIdAndCompanyId(warehouseId, COMPANY_ID)).thenReturn(Optional.of(warehouse));
        when(locationRepository.findAllByWarehouseIdOrderByCodeAsc(warehouseId)).thenReturn(locations);

        List<LocationResponse> result = locationService.listByWarehouse(warehouseId);

        assertEquals(2, result.size());
        assertEquals("A1", result.get(0).code());
        assertEquals("A2", result.get(1).code());
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar localizações de depósito inexistente")
    void shouldThrowNotFoundWhenListingLocationsForNonExistentWarehouse() {
        UUID warehouseId = UUID.randomUUID();
        when(warehouseRepository.findByIdAndCompanyId(warehouseId, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> locationService.listByWarehouse(warehouseId));
        verifyNoInteractions(locationRepository);
    }

    @Test
    @DisplayName("Deve encontrar localização pelo ID")
    void shouldFindLocationById() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = buildWarehouse(UUID.randomUUID());
        Location location = buildLocation(id, warehouse, "B3");
        when(locationRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(location));

        LocationResponse response = locationService.findById(id);

        assertEquals(id, response.id());
        assertEquals("B3", response.code());
    }

    @Test
    @DisplayName("Deve lançar exceção quando localização não existe pelo ID")
    void shouldThrowNotFoundWhenLocationDoesNotExistById() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> locationService.findById(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("Deve atualizar código e descrição da localização")
    void shouldUpdateLocationCodeAndDescription() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = buildWarehouse(UUID.randomUUID());
        Location existing = buildLocation(id, warehouse, "Antigo");
        LocationRequest request = new LocationRequest("Novo Código", "Nova Descrição");
        Location updated = new Location(id, warehouse, "Novo Código", "Nova Descrição");

        when(locationRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(existing));
        when(locationRepository.save(any(Location.class))).thenReturn(updated);

        LocationResponse response = locationService.update(id, request);

        assertEquals("Novo Código", response.code());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar localização inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistentLocation() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> locationService.update(id, new LocationRequest("X", null)));
        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve excluir localização com sucesso")
    void shouldDeleteLocationSuccessfully() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = buildWarehouse(UUID.randomUUID());
        Location existing = buildLocation(id, warehouse, "DEL-01");
        when(locationRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(existing));

        locationService.delete(id);

        verify(locationRepository).delete(existing);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir localização inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistentLocation() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> locationService.delete(id));
        verify(locationRepository, never()).delete(any());
    }
}
