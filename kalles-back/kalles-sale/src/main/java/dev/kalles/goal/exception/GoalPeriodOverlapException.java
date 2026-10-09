package dev.kalles.goal.exception;

import dev.kalles.goal.enums.Periodicity;
import dev.kalles.shared.exception.ConflictException;

public class GoalPeriodOverlapException extends ConflictException {

    public GoalPeriodOverlapException(Periodicity periodicity) {
        super("GOAL_PERIOD_OVERLAP", "There is already an active " + periodicity + " Goal overlapping the given period");
    }
}
