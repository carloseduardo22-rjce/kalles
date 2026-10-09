package dev.kalles.goal.exception;

import dev.kalles.shared.exception.ConflictException;

public class ActiveGoalNotEditableException extends ConflictException {

    public ActiveGoalNotEditableException(String message) {
        super("GOAL_ACTIVE_NOT_EDITABLE", message);
    }
}
