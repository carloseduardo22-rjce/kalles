package dev.kalles.core.service;

import dev.kalles.core.dto.FidelityPolicyRequest;
import dev.kalles.core.dto.FidelityPolicyResponse;
import dev.kalles.core.entity.FidelityPolicy;
import dev.kalles.core.enums.fidelity.FidelityDiscountType;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.FidelityPolicyRepository;
import dev.kalles.security.context.CompanyContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    @Mock
    private FidelityPolicyRepository fidelityPolicyRepository;

    @InjectMocks
    private FidelityPolicyService fidelityPolicyService;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    private FidelityPolicy buildPolicy(boolean active) {
        FidelityPolicy p = new FidelityPolicy();
        p.setId(UUID.randomUUID());
        p.setObjectivePoints(100);
        p.setConfiguredDiscount(new BigDecimal("20.00"));
        p.setValuePoint(1);
        p.setDiscountType(FidelityDiscountType.FIXED);
        p.setActive(active);
        p.setCreatedAt(LocalDate.now());
        return p;
    }

    @Test
    @DisplayName("Deve criar política com sucesso e desativar as anteriores")
    void shouldCreatePolicySuccessfully() {
        FidelityPolicyRequest request = new FidelityPolicyRequest(
                100,
                new BigDecimal("20.00"),
                1,
                FidelityDiscountType.FIXED);
        FidelityPolicy saved = buildPolicy(true);

        when(fidelityPolicyRepository.save(any(FidelityPolicy.class))).thenReturn(saved);

        FidelityPolicyResponse response = fidelityPolicyService.create(request);

        assertNotNull(response);
        assertEquals(100, response.objectivePoints());
        assertEquals(new BigDecimal("20.00"), response.configuredDiscount());
        assertEquals(FidelityDiscountType.FIXED, response.discountType());
        assertTrue(response.active());
        verify(fidelityPolicyRepository).deactivateAllByCompanyId(COMPANY_ID);
        verify(fidelityPolicyRepository).save(any(FidelityPolicy.class));
    }

    @Test
    @DisplayName("Deve desativar políticas existentes ao criar nova")
    void shouldDeactivateExistingPoliciesWhenCreatingNew() {
        FidelityPolicyRequest request = new FidelityPolicyRequest(
                200,
                new BigDecimal("50.00"),
                2,
                FidelityDiscountType.PERCENTAGE);
        FidelityPolicy saved = buildPolicy(true);
        saved.setObjectivePoints(200);
        saved.setConfiguredDiscount(new BigDecimal("50.00"));
        saved.setValuePoint(2);
        saved.setDiscountType(FidelityDiscountType.PERCENTAGE);

        when(fidelityPolicyRepository.save(any(FidelityPolicy.class))).thenReturn(saved);

        fidelityPolicyService.create(request);

        verify(fidelityPolicyRepository).deactivateAllByCompanyId(COMPANY_ID);
    }

    @Test
    @DisplayName("Deve retornar a política ativa quando existir")
    void shouldReturnActivePolicyWhenExists() {
        FidelityPolicy active = buildPolicy(true);
        when(fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(COMPANY_ID)).thenReturn(Optional.of(active));

        FidelityPolicyResponse response = fidelityPolicyService.getActive();

        assertNotNull(response);
        assertTrue(response.active());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não existe política ativa")
    void shouldThrowNotFoundWhenNoActivePolicyExists() {
        when(fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fidelityPolicyService.getActive());
    }

    @Test
    @DisplayName("Deve listar todas as políticas cadastradas")
    void shouldListAllPolicies() {
        List<FidelityPolicy> policies = List.of(buildPolicy(false), buildPolicy(true));
        when(fidelityPolicyRepository.findAllByCompanyIdOrderByCreatedAtDesc(COMPANY_ID)).thenReturn(policies);

        List<FidelityPolicyResponse> result = fidelityPolicyService.listAll();

        assertEquals(2, result.size());
    }
}
