package dev.kalles.support.domain.exception;

/**
 * Thrown by the State Pattern when a requested state transition
 * is not permitted from the current Ticket status.
 */
public class InvalidStateTransitionException extends TicketDomainException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
