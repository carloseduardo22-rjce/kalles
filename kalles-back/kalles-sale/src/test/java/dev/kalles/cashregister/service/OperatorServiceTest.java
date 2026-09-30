package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.OperatorRequest;
import dev.kalles.cashregister.dto.OperatorResponse;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.exception.OperatorCodeAlreadyExistsException;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.security.exception.CompanyContextRequiredException;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.testsupport.RequestContextExtension;
import dev.kalles.security.context.RequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
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
@DisplayName("OperatorService - Servico de Operadores")
class OperatorServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("7ec7d531-95a4-4f19-b989-459e2c1ea701");

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.company(COMPANY_ID);

    @Mock
    private OperatorRepository operatorRepository;

    @InjectMocks
    private OperatorService operatorService;

    @Test
    @DisplayName("Deve criar operador vinculado a filial ativa")
    void shouldCreateOperatorForActiveCompany() {
        OperatorRequest request = new OperatorRequest("Maria Silva", "maria.silva", PermissionLevel.MANAGER);
        Operator saved = buildOperator(UUID.randomUUID(), "Maria Silva", "maria.silva", PermissionLevel.MANAGER, true);

        when(operatorRepository.findByCodeAndCompanyId("maria.silva", COMPANY_ID)).thenReturn(Optional.empty());
        when(operatorRepository.save(any(Operator.class))).thenReturn(saved);

        OperatorResponse response = operatorService.create(request);

        assertEquals("Maria Silva", response.name());
        assertEquals("maria.silva", response.code());
        assertEquals("MANAGER", response.permissionLevel());

        ArgumentCaptor<Operator> captor = ArgumentCaptor.forClass(Operator.class);
        verify(operatorRepository).save(captor.capture());
        assertEquals(COMPANY_ID, captor.getValue().getCompanyId());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    @DisplayName("Deve rejeitar codigo duplicado na mesma filial")
    void shouldRejectDuplicateCodeWithinSameCompany() {
        OperatorRequest request = new OperatorRequest("Maria Silva", "maria.silva", PermissionLevel.MANAGER);
        when(operatorRepository.findByCodeAndCompanyId("maria.silva", COMPANY_ID))
                .thenReturn(Optional.of(buildOperator(UUID.randomUUID(), "Outra Maria", "maria.silva", PermissionLevel.BASIC, true)));

        OperatorCodeAlreadyExistsException exception = assertThrows(OperatorCodeAlreadyExistsException.class, () -> operatorService.create(request));

        assertEquals("OPERATOR_CODE_ALREADY_EXISTS", exception.getCode());
        verify(operatorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar edicao para codigo de outro operador da filial")
    void shouldRejectUpdateToCodeOfAnotherOperator() {
        UUID operatorId = UUID.randomUUID();
        OperatorRequest request = new OperatorRequest("Maria Silva", "maria.silva", PermissionLevel.MANAGER);
        when(operatorRepository.findByIdAndCompanyId(operatorId, COMPANY_ID))
                .thenReturn(Optional.of(buildOperator(operatorId, "Maria Silva", "maria", PermissionLevel.MANAGER, true)));
        when(operatorRepository.findByCodeAndCompanyId("maria.silva", COMPANY_ID))
                .thenReturn(Optional.of(buildOperator(UUID.randomUUID(), "Outra Maria", "maria.silva", PermissionLevel.BASIC, true)));

        OperatorCodeAlreadyExistsException exception = assertThrows(OperatorCodeAlreadyExistsException.class, () -> operatorService.update(operatorId, request));

        assertEquals("OPERATOR_CODE_ALREADY_EXISTS", exception.getCode());
        verify(operatorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar apenas operadores ativos da filial")
    void shouldListOnlyActiveOperatorsFromCurrentCompany() {
        when(operatorRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(COMPANY_ID)).thenReturn(List.of(
                buildOperator(UUID.randomUUID(), "Ana", "ana", PermissionLevel.BASIC, true),
                buildOperator(UUID.randomUUID(), "Carlos", "carlos", PermissionLevel.SUPERVISOR, true)
        ));

        List<OperatorResponse> responses = operatorService.listAll();

        assertEquals(2, responses.size());
        verify(operatorRepository).findAllByCompanyIdAndActiveTrueOrderByNameAsc(COMPANY_ID);
    }

    @Test
    @DisplayName("Deve buscar operador apenas dentro da filial ativa")
    void shouldFindOperatorWithinCurrentCompany() {
        UUID operatorId = UUID.randomUUID();
        when(operatorRepository.findByIdAndCompanyId(operatorId, COMPANY_ID))
                .thenReturn(Optional.of(buildOperator(operatorId, "Ana", "ana", PermissionLevel.BASIC, true)));

        OperatorResponse response = operatorService.findById(operatorId);

        assertEquals(operatorId, response.id());
        assertEquals("ana", response.code());
    }

    @Test
    @DisplayName("Deve falhar ao buscar operador de outra filial")
    void shouldThrowNotFoundWhenOperatorDoesNotBelongToCurrentCompany() {
        UUID operatorId = UUID.randomUUID();
        when(operatorRepository.findByIdAndCompanyId(operatorId, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> operatorService.findById(operatorId));
    }

    @Test
    @DisplayName("Deve desativar operador por soft delete")
    void shouldDeactivateOperator() {
        UUID operatorId = UUID.randomUUID();
        Operator operator = buildOperator(operatorId, "Ana", "ana", PermissionLevel.BASIC, true);
        when(operatorRepository.findByIdAndCompanyId(operatorId, COMPANY_ID)).thenReturn(Optional.of(operator));

        operatorService.deactivate(operatorId);

        assertFalse(operator.isActive());
        verify(operatorRepository).save(operator);
    }

    @Test
    @DisplayName("Deve exigir filial ativa no contexto")
    void shouldRequireCompanyContext() {
        RequestContext.runWithin(RequestContext.empty(), () ->
                assertThrows(CompanyContextRequiredException.class, () -> operatorService.listAll()));
        verifyNoInteractions(operatorRepository);
    }

    private Operator buildOperator(UUID id, String name, String code, PermissionLevel permissionLevel, boolean active) {
        Operator operator = new Operator();
        operator.setId(id);
        operator.setCompanyId(COMPANY_ID);
        operator.setName(name);
        operator.setCode(code);
        operator.setPermissionLevel(permissionLevel);
        operator.setActive(active);
        return operator;
    }
}
