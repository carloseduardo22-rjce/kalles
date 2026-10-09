package dev.kalles.support.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class TicketNotAssignedException extends BusinessRuleViolationException {

    public TicketNotAssignedException() {
        super("SUPPORT_TICKET_NOT_ASSIGNED", "The ticket must be assigned before an agent can reply");
    }
}
