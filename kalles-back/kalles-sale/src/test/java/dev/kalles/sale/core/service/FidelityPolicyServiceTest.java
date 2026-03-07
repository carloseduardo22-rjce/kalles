package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.FidelityPolicyRequest;
import dev.kalles.sale.core.dto.FidelityPolicyResponse;
import dev.kalles.sale.core.entity.FidelityPolicy;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.FidelityPolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FidelityPolicyService - Serviço de Política de Fidelidade")
class FidelityPolicyServiceTest {

    @Mock
    private FidelityPolicyRepository fidelityPolicyRepository;

    @InjectMocks
    private FidelityPolicyService fidelityPolicyService;

    private FidelityPolicy buildPolicy(boolean active) {
        FidelityPolicy p = new FidelityPolicy();
        p.setId(UUID.randomUUID());
        p.setObjectivePoints(100);
        p.setConfiguredDiscount(new BigDecimal("20.00"));
        p.setValuePoint(1);
        p.setActive(active);
        p.setCreatedAt(LocalDate.now());
        return p;
    }

    @Test
    @DisplayName("Deve criar política com sucesso e desativar as anteriores")
    void shouldCreatePolicySuccessfully() {
        FidelityPolicyRequest request = new FidelityPolicyRequest(100, new BigDecimal("20.00"), 1);
        FidelityPolicy saved = buildPolicy(true);

        when(fidelityPolicyRepository.save(any(FidelityPolicy.class))).thenReturn(saved);

        FidelityPolicyResponse response = fidelityPolicyService.create(request);

        assertNotNull(response);
        assertEquals(100, response.objectivePoints());
        assertEquals(new BigDecimal("20.00"), response.configuredDiscount());
        assertTrue(response.active());
        verify(fidelityPolicyRepository).deactivateAll();
        verify(fidelityPolicyRepository).save(any(FidelityPolicy.class));
    }

    @Test
    @DisplayName("Deve desativar políticas existentes ao criar nova")
    void shouldDeactivateExistingPoliciesWhenCreatingNew() {
        FidelityPolicyRequest request = new FidelityPolicyRequest(200, new BigDecimal("50.00"), 2);
        FidelityPolicy saved = buildPolicy(true);
        saved.setObjectivePoints(200);
        saved.setConfiguredDiscount(new BigDecimal("50.00"));
        saved.setValuePoint(2);

        when(fidelityPolicyRepository.save(any(FidelityPolicy.class))).thenReturn(saved);

        fidelityPolicyService.create(request);

        verify(fidelityPolicyRepository).deactivateAll();
    }

    @Test
    @DisplayName("Deve retornar a política ativa quando existir")
    void shouldReturnActivePolicyWhenExists() {
        FidelityPolicy active = buildPolicy(true);
        when(fidelityPolicyRepository.findFirstByActiveTrue()).thenReturn(Optional.of(active));

        FidelityPolicyResponse response = fidelityPolicyService.getActive();

        assertNotNull(response);
        assertTrue(response.active());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não existe política ativa")
    void shouldThrowNotFoundWhenNoActivePolicyExists() {
        when(fidelityPolicyRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fidelityPolicyService.getActive());
    }

    @Test
    @DisplayName("Deve listar todas as políticas cadastradas")
    void shouldListAllPolicies() {
        List<FidelityPolicy> policies = List.of(buildPolicy(false), buildPolicy(true));
        when(fidelityPolicyRepository.findAll()).thenReturn(policies);

        List<FidelityPolicyResponse> result = fidelityPolicyService.listAll();

        assertEquals(2, result.size());
    }
}
