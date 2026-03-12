package dev.kalles.sale.core.service;

import dev.kalles.sale.core.entity.Goal;
import dev.kalles.sale.core.enums.goal.GoalStatus;
import dev.kalles.sale.core.exception.GoalDomainException;

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
