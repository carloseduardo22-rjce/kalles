package dev.kalles.goal.service;

import dev.kalles.goal.entity.Goal;
import dev.kalles.goal.enums.GoalStatus;
import dev.kalles.goal.exception.GoalPeriodOverlapException;

import java.util.List;

public class OverlapValidator {

    private OverlapValidator() {
    }

    public static void validate(List<Goal> existingGoals, Goal incoming) {
        existingGoals.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .filter(g -> g.getPeriodicity() == incoming.getPeriodicity())
                .filter(g -> g.overlaps(incoming))
                .findFirst()
                .ifPresent(g -> {
                    throw new GoalPeriodOverlapException(incoming.getPeriodicity());
                });
    }
}
