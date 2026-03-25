package dev.kalles.support.api.ticket;

import dev.kalles.support.application.dto.AssignTicketRequest;
import dev.kalles.support.application.dto.OpenTicketRequest;
import dev.kalles.support.application.dto.TicketResponse;
import dev.kalles.support.application.service.TicketService;
import dev.kalles.support.domain.TicketStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Lifecycle management of helpdesk support tickets")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    @Operation(
        summary = "List tickets",
        description = "Returns all tickets ordered by creation date (newest first). Use ?status= to filter by status."
    )
    public ResponseEntity<List<TicketResponse>> listAll(
            @RequestParam(required = false) TicketStatus status) {
        return ResponseEntity.ok(
                ticketService.listAll(Optional.ofNullable(status))
                        .stream()
                        .map(TicketResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket found"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(TicketResponse.from(ticketService.findById(id)));
    }

    @PostMapping
    @Operation(
        summary = "Open a new ticket",
        description = "Creates a new support ticket. Priority is automatically derived from the category. The user is created if not found by email."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket opened successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<TicketResponse> openTicket(@Valid @RequestBody OpenTicketRequest request) {
        var ticket = ticketService.openTicket(
                request.title(),
                request.description(),
                request.userEmail(),
                request.userName(),
                request.categoryId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }

    @PatchMapping("/{id}/assign")
    @Operation(
        summary = "Assign an agent",
        description = "Assigns an agent to an OPEN ticket, transitioning it to IN_PROGRESS. Throws 409 if the ticket is not in OPEN state."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agent assigned successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket or agent not found"),
        @ApiResponse(responseCode = "409", description = "Ticket is not in OPEN state")
    })
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request) {
        return ResponseEntity.ok(TicketResponse.from(ticketService.assignTicket(id, request.agentId())));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "List tickets by agent", description = "Returns tickets assigned to a specific agent.")
    public ResponseEntity<List<TicketResponse>> findByAgent(@PathVariable UUID agentId) {
        return ResponseEntity.ok(
                ticketService.findByAgent(agentId).stream().map(TicketResponse::from).toList());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List tickets by user", description = "Returns tickets opened by a specific user.")
    public ResponseEntity<List<TicketResponse>> findByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                ticketService.findByUser(userId).stream().map(TicketResponse::from).toList());
    }
}
