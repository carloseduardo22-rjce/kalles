package dev.kalles.sale.goal.service;

import dev.kalles.sale.goal.entity.Goal;
import dev.kalles.sale.goal.enums.GoalStatus;
import dev.kalles.sale.goal.exception.GoalDomainException;

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
                    throw new GoalDomainException(
                            "There is already an active " + incoming.getPeriodicity() + " Goal overlapping the given period"
                    );
                });
    }
}
