package dev.kalles.support.domain;

/**
 * State Pattern interface for the Ticket lifecycle.
 * <p>
 * Each concrete implementation represents one valid state and defines
 * which transitions are allowed from it. Invalid transitions throw
 * InvalidStateTransitionException.
 * <p>
 * Package-private: this is an implementation detail of the domain.
 * External code interacts exclusively through the public behaviours of Ticket.
 */
interface TicketState {

    void assign(Ticket ticket, Agent agent);
}
