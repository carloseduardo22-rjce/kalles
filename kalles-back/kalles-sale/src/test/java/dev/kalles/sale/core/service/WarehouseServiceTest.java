package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.WarehouseRequest;
import dev.kalles.sale.core.dto.WarehouseResponse;
import dev.kalles.sale.core.entity.Warehouse;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void shouldCreateWarehouseWithActiveTrueByDefault() {
        WarehouseRequest request = new WarehouseRequest("Depósito Central", "Rua das Flores, 100");
        Warehouse saved = new Warehouse(UUID.randomUUID(), "Depósito Central", "Rua das Flores, 100", true);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertNotNull(response);
        assertEquals("Depósito Central", response.name());
        assertTrue(response.active());

        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive(), "Novo depósito deve ser criado como ativo");
    }

    @Test
    void shouldPersistNameAndAddressOnCreate() {
        WarehouseRequest request = new WarehouseRequest("Dep A", "Av. Brasil, 10");
        Warehouse saved = new Warehouse(UUID.randomUUID(), "Dep A", "Av. Brasil, 10", true);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertEquals("Dep A", response.name());
        assertEquals("Av. Brasil, 10", response.address());
    }

    @Test
    void shouldReturnOnlyActiveWarehouses() {
        Warehouse w1 = new Warehouse(UUID.randomUUID(), "Dep A", null, true);
        Warehouse w2 = new Warehouse(UUID.randomUUID(), "Dep B", null, true);
        when(warehouseRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(w1, w2));

        List<WarehouseResponse> result = warehouseService.listActiveWarehouses();

        assertEquals(2, result.size());
        verify(warehouseRepository).findAllByActiveTrueOrderByNameAsc();
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveWarehouses() {
        when(warehouseRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        List<WarehouseResponse> result = warehouseService.listActiveWarehouses();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindWarehouseById() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = new Warehouse(id, "Dep X", "Rua Y", true);
        when(warehouseRepository.findById(id)).thenReturn(Optional.of(warehouse));

        WarehouseResponse response = warehouseService.findWarehouseById(id);

        assertEquals(id, response.id());
        assertEquals("Dep X", response.name());
    }

    @Test
    void shouldThrowNotFoundWhenWarehouseDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(warehouseRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> warehouseService.findWarehouseById(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void shouldUpdateWarehouseNameAndAddress() {
        UUID id = UUID.randomUUID();
        Warehouse existing = new Warehouse(id, "Nome Antigo", "End. Antigo", true);
        WarehouseRequest request = new WarehouseRequest("Nome Novo", "End. Novo");
        Warehouse updated = new Warehouse(id, "Nome Novo", "End. Novo", true);

        when(warehouseRepository.findById(id)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updated);

        WarehouseResponse response = warehouseService.updateWarehouse(id, request);

        assertEquals("Nome Novo", response.name());
        assertEquals("End. Novo", response.address());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentWarehouse() {
        UUID id = UUID.randomUUID();
        when(warehouseRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> warehouseService.updateWarehouse(id, new WarehouseRequest("X", null)));
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void shouldSetActiveToFalseOnDeactivate() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = new Warehouse(id, "Dep Z", null, true);
        when(warehouseRepository.findById(id)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any())).thenReturn(warehouse);

        warehouseService.deactivateWarehouse(id);

        assertFalse(warehouse.isActive());
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void shouldThrowNotFoundWhenDeactivatingNonExistentWarehouse() {
        UUID id = UUID.randomUUID();
        when(warehouseRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> warehouseService.deactivateWarehouse(id));
        verify(warehouseRepository, never()).save(any());
    }
}
