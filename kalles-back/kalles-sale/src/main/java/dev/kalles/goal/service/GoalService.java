package dev.kalles.goal.service;

import dev.kalles.goal.dto.GoalRequest;
import dev.kalles.goal.dto.GoalResponse;
import dev.kalles.goal.entity.Goal;
import dev.kalles.goal.enums.GoalStatus;
import dev.kalles.goal.repository.GoalRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalAssessmentService goalAssessmentService;

    @Transactional
    public GoalResponse create(GoalRequest request) {
        UUID companyId = getCompanyId();
        Goal goal = Goal.create(companyId, request.targetValue(), request.periodicity(), request.startDate(), request.endDate());
        List<Goal> conflicting = goalRepository.findByCompanyIdAndPeriodicityAndStatus(companyId, goal.getPeriodicity(), GoalStatus.ACTIVE);
        OverlapValidator.validate(conflicting, goal);
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> listAll() {
        return goalRepository.findAllByCompanyIdOrderByStartDateDesc(getCompanyId()).stream()
                .map(GoalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse findById(UUID id) {
        return goalRepository.findByIdAndCompanyId(id, getCompanyId())
                .map(GoalResponse::from)
                .orElseThrow(() -> new NotFoundException("Meta não encontrada: " + id));
    }

    @Transactional
    public GoalResponse update(UUID id, GoalRequest request) {
        Goal goal = getGoalOrThrow(id);
        goal.changeTargetValue(request.targetValue());
        goal.changePeriod(request.startDate(), request.endDate());
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse activate(UUID id) {
        UUID companyId = getCompanyId();
        Goal goal = getGoalOrThrow(id);
        List<Goal> conflicting = goalRepository.findByCompanyIdAndPeriodicityAndStatus(companyId, goal.getPeriodicity(), GoalStatus.ACTIVE);
        OverlapValidator.validate(conflicting, goal);
        goal.activate();
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse close(UUID id) {
        Goal goal = getGoalOrThrow(id);
        goal.close();
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public void delete(UUID id) {
        Goal goal = getGoalOrThrow(id);
        goalRepository.delete(goal);
    }

    @Transactional(readOnly = true)
    public GoalAssessmentResult assess(UUID id, BigDecimal totalSold) {
        Goal goal = getGoalOrThrow(id);
        return goalAssessmentService.assess(goal, totalSold);
    }

    @Transactional(readOnly = true)
    public GoalAssessmentResult getProgress(UUID id) {
        Goal goal = getGoalOrThrow(id);
        return goalAssessmentService.autoAssess(goal);
    }

    private Goal getGoalOrThrow(UUID id) {
        return goalRepository.findByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new NotFoundException("Meta não encontrada: " + id));
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
    }
}
