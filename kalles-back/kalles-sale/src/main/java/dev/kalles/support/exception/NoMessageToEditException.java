package dev.kalles.support.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class NoMessageToEditException extends BusinessRuleViolationException {

    public NoMessageToEditException() {
        super("SUPPORT_NO_MESSAGE_TO_EDIT", "There is no message available to edit");
    }
}
