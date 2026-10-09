package dev.kalles.support.exception;

import dev.kalles.shared.exception.ConflictException;

public class InvalidStateTransitionException extends ConflictException {

    public InvalidStateTransitionException(String message) {
        super("SUPPORT_TICKET_INVALID_TRANSITION", message);
    }
}
