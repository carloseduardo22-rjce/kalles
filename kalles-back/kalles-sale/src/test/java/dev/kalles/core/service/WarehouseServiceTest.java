package dev.kalles.core.service;

import dev.kalles.core.dto.WarehouseRequest;
import dev.kalles.core.dto.WarehouseResponse;
import dev.kalles.core.entity.Warehouse;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.WarehouseRepository;
import dev.kalles.security.context.CompanyContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("WarehouseService - Serviço de Depósitos")
class WarehouseServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve criar depósito como ativo por padrão")
    void shouldCreateWarehouseWithActiveTrueByDefault() {
        WarehouseRequest request = new WarehouseRequest("Depósito Central", "Rua das Flores, 100");
        Warehouse saved = new Warehouse(UUID.randomUUID(), "Depósito Central", COMPANY_ID, "Rua das Flores, 100", true);
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
    @DisplayName("Deve persistir nome e endereço ao criar o depósito")
    void shouldPersistNameAndAddressOnCreate() {
        WarehouseRequest request = new WarehouseRequest("Dep A", "Av. Brasil, 10");
        Warehouse saved = new Warehouse(UUID.randomUUID(), "Dep A", COMPANY_ID, "Av. Brasil, 10", true);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertEquals("Dep A", response.name());
        assertEquals("Av. Brasil, 10", response.address());
    }

    @Test
    @DisplayName("Deve retornar apenas os depósitos ativos")
    void shouldReturnOnlyActiveWarehouses() {
        Warehouse w1 = new Warehouse(UUID.randomUUID(), "Dep A", COMPANY_ID, null, true);
        Warehouse w2 = new Warehouse(UUID.randomUUID(), "Dep B", COMPANY_ID, null, true);
        when(warehouseRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(COMPANY_ID)).thenReturn(List.of(w1, w2));

        List<WarehouseResponse> result = warehouseService.listActiveWarehouses();

        assertEquals(2, result.size());
        verify(warehouseRepository).findAllByCompanyIdAndActiveTrueOrderByNameAsc(COMPANY_ID);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há depósitos ativos")
    void shouldReturnEmptyListWhenNoActiveWarehouses() {
        when(warehouseRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(COMPANY_ID)).thenReturn(List.of());

        List<WarehouseResponse> result = warehouseService.listActiveWarehouses();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve encontrar depósito pelo ID")
    void shouldFindWarehouseById() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = new Warehouse(id, "Dep X", COMPANY_ID, "Rua Y", true);
        when(warehouseRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(warehouse));

        WarehouseResponse response = warehouseService.findWarehouseById(id);

        assertEquals(id, response.id());
        assertEquals("Dep X", response.name());
    }

    @Test
    @DisplayName("Deve lançar exceção quando depósito não existe")
    void shouldThrowNotFoundWhenWarehouseDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(warehouseRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> warehouseService.findWarehouseById(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("Deve atualizar nome e endereço do depósito")
    void shouldUpdateWarehouseNameAndAddress() {
        UUID id = UUID.randomUUID();
        Warehouse existing = new Warehouse(id, "Nome Antigo", COMPANY_ID, "End. Antigo", true);
        WarehouseRequest request = new WarehouseRequest("Nome Novo", "End. Novo");
        Warehouse updated = new Warehouse(id, "Nome Novo", COMPANY_ID, "End. Novo", true);

        when(warehouseRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updated);

        WarehouseResponse response = warehouseService.updateWarehouse(id, request);

        assertEquals("Nome Novo", response.name());
        assertEquals("End. Novo", response.address());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar depósito inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistentWarehouse() {
        UUID id = UUID.randomUUID();
        when(warehouseRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> warehouseService.updateWarehouse(id, new WarehouseRequest("X", null)));
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve marcar depósito como inativo ao desativá-lo")
    void shouldSetActiveToFalseOnDeactivate() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = new Warehouse(id, "Dep Z", COMPANY_ID, null, true);
        when(warehouseRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any())).thenReturn(warehouse);

        warehouseService.deactivateWarehouse(id);

        assertFalse(warehouse.isActive());
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    @DisplayName("Deve lançar exceção ao desativar depósito inexistente")
    void shouldThrowNotFoundWhenDeactivatingNonExistentWarehouse() {
        UUID id = UUID.randomUUID();
        when(warehouseRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> warehouseService.deactivateWarehouse(id));
        verify(warehouseRepository, never()).save(any());
    }
}
