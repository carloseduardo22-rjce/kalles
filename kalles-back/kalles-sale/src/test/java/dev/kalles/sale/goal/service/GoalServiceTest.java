package dev.kalles.sale.goal.service;

import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.goal.dto.GoalRequest;
import dev.kalles.sale.goal.dto.GoalResponse;
import dev.kalles.sale.goal.entity.Goal;
import dev.kalles.sale.goal.enums.GoalStatus;
import dev.kalles.sale.goal.enums.Periodicity;
import dev.kalles.sale.goal.exception.GoalDomainException;
import dev.kalles.sale.goal.repository.GoalRepository;
import dev.kalles.sale.goal.service.GoalAssessmentService;
import dev.kalles.sale.goal.service.GoalService;
import dev.kalles.sale.security.context.CompanyContextHolder;
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
@DisplayName("GoalService - Servico de Metas")
class GoalServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("1d2d8778-e0cd-4d6b-9f0d-f186cb11a301");

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalAssessmentService goalAssessmentService;

    @InjectMocks
    private GoalService goalService;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve criar meta em rascunho para a filial ativa")
    void shouldCreateDraftGoalForActiveCompany() {
        GoalRequest request = new GoalRequest(
                new BigDecimal("10000.00"),
                Periodicity.MONTHLY,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        );
        Goal saved = Goal.create(COMPANY_ID, request.targetValue(), request.periodicity(), request.startDate(), request.endDate());

        when(goalRepository.findByCompanyIdAndPeriodicityAndStatus(COMPANY_ID, Periodicity.MONTHLY, GoalStatus.ACTIVE))
                .thenReturn(List.of());
        when(goalRepository.save(any(Goal.class))).thenReturn(saved);

        GoalResponse response = goalService.create(request);

        assertEquals(GoalStatus.DRAFT, response.status());
        assertEquals(new BigDecimal("10000.00"), response.targetValue());
    }

    @Test
    @DisplayName("Deve bloquear criacao de meta com sobreposicao a meta ativa da mesma filial")
    void shouldRejectOverlappingActiveGoalWithinSameCompany() {
        GoalRequest request = new GoalRequest(
                new BigDecimal("10000.00"),
                Periodicity.MONTHLY,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        );
        Goal activeGoal = Goal.create(COMPANY_ID, new BigDecimal("9000.00"), Periodicity.MONTHLY,
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 25));
        activeGoal.activate();

        when(goalRepository.findByCompanyIdAndPeriodicityAndStatus(COMPANY_ID, Periodicity.MONTHLY, GoalStatus.ACTIVE))
                .thenReturn(List.of(activeGoal));

        assertThrows(GoalDomainException.class, () -> goalService.create(request));
        verify(goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar meta apenas dentro da filial ativa")
    void shouldFindGoalWithinCurrentCompany() {
        UUID goalId = UUID.randomUUID();
        Goal goal = Goal.create(COMPANY_ID, new BigDecimal("10000.00"), Periodicity.MONTHLY,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        when(goalRepository.findByIdAndCompanyId(goalId, COMPANY_ID)).thenReturn(Optional.of(goal));

        GoalResponse response = goalService.findById(goalId);

        assertEquals(GoalStatus.DRAFT, response.status());
    }

    @Test
    @DisplayName("Deve falhar ao buscar meta de outra filial")
    void shouldThrowNotFoundWhenGoalDoesNotBelongToCurrentCompany() {
        UUID goalId = UUID.randomUUID();
        when(goalRepository.findByIdAndCompanyId(goalId, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> goalService.findById(goalId));
    }

    @Test
    @DisplayName("Deve exigir filial ativa no contexto")
    void shouldRequireCompanyContext() {
        CompanyContextHolder.clear();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> goalService.listAll());

        assertEquals("Nenhuma filial selecionada no contexto da operação.", exception.getMessage());
        verifyNoInteractions(goalRepository);
    }
}
