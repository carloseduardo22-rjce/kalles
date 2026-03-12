package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.GoalRequest;
import dev.kalles.sale.core.dto.GoalResponse;
import dev.kalles.sale.core.entity.Goal;
import dev.kalles.sale.core.enums.goal.GoalStatus;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.GoalRepository;
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
        Goal goal = Goal.create(request.targetValue(), request.periodicity(), request.startDate(), request.endDate());
        List<Goal> conflicting = goalRepository.findByPeriodicityAndStatus(goal.getPeriodicity(), GoalStatus.ACTIVE);
        OverlapValidator.validate(conflicting, goal);
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> listAll() {
        return goalRepository.findAll().stream()
                .map(GoalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse findById(UUID id) {
        return goalRepository.findById(id)
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
        Goal goal = getGoalOrThrow(id);
        List<Goal> conflicting = goalRepository.findByPeriodicityAndStatus(goal.getPeriodicity(), GoalStatus.ACTIVE);
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
        if (!goalRepository.existsById(id)) {
            throw new NotFoundException("Meta não encontrada: " + id);
        }
        goalRepository.deleteById(id);
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
        return goalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Meta não encontrada: " + id));
    }
}
