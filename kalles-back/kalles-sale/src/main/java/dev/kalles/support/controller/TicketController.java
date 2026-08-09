package dev.kalles.support.controller;

import dev.kalles.shared.dto.PageResponse;
import dev.kalles.support.domain.TicketStatus;
import dev.kalles.support.dto.*;
import dev.kalles.support.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Lifecycle management of helpdesk support tickets")
public class TicketController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final TicketService ticketService;

    @GetMapping
    @Operation(summary = "List accessible tickets")
    public ResponseEntity<List<TicketResponse>> listAll(
            @RequestParam(required = false) TicketStatus status,
            Authentication authentication) {
        return ResponseEntity.ok(
                ticketService.listAccessible(Optional.ofNullable(status), authentication.getName(), isAdmin(authentication))
                        .stream()
                        .map(TicketResponse::from)
                        .toList()
        );
    }

    @GetMapping("/page")
    @Operation(summary = "List accessible tickets with pagination")
    public ResponseEntity<PageResponse<TicketResponse>> listPage(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return ResponseEntity.ok(PageResponse.from(
                ticketService.listAccessiblePage(
                                Optional.ofNullable(status),
                                authentication.getName(),
                                isAdmin(authentication),
                                page,
                                size
                        )
                        .map(TicketResponse::from)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accessible ticket by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket found"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> findById(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(TicketResponse.from(
                ticketService.findAccessibleById(id, authentication.getName(), isAdmin(authentication))
        ));
    }

    @PostMapping
    @Operation(summary = "Open a new ticket")
    public ResponseEntity<TicketResponse> openTicket(
            @Valid @RequestBody OpenTicketRequest request,
            Authentication authentication) {
        var ticket = ticketService.openTicket(
                request.title(),
                request.description(),
                request.categoryId(),
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign an agent")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request,
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(TicketResponse.from(ticketService.assignTicket(id, request.agentId())));
    }

    @PostMapping("/{id}/customer-message")
    @Operation(summary = "Send a customer message")
    public ResponseEntity<TicketResponse> addCustomerMessage(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerMessageRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(TicketResponse.from(
                ticketService.addCustomerMessage(id, request.content(), authentication.getName())
        ));
    }

    @PatchMapping("/{id}/customer-message")
    @Operation(summary = "Edit the latest customer message")
    public ResponseEntity<TicketResponse> editCustomerMessage(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerMessageRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(TicketResponse.from(
                ticketService.editLastCustomerMessage(id, request.content(), authentication.getName())
        ));
    }

    @PostMapping("/{id}/agent-message")
    @Operation(summary = "Send an agent message")
    public ResponseEntity<TicketResponse> addAgentMessage(
            @PathVariable UUID id,
            @Valid @RequestBody AgentMessageRequest request,
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(TicketResponse.from(
                ticketService.addAgentMessage(id, request.content(), request.markAsResolved())
        ));
    }

    @PatchMapping("/{id}/agent-message")
    @Operation(summary = "Edit the latest agent message")
    public ResponseEntity<TicketResponse> editAgentMessage(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerMessageRequest request,
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(TicketResponse.from(
                ticketService.editLastAgentMessage(id, request.content())
        ));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close a resolved ticket")
    public ResponseEntity<TicketResponse> closeTicket(
            @PathVariable UUID id,
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(TicketResponse.from(ticketService.closeTicket(id)));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "List tickets by agent")
    public ResponseEntity<List<TicketResponse>> findByAgent(@PathVariable UUID agentId, Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(
                ticketService.findByAgent(agentId).stream().map(TicketResponse::from).toList());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List tickets by user")
    public ResponseEntity<List<TicketResponse>> findByUser(@PathVariable UUID userId, Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(
                ticketService.findByUser(userId).stream().map(TicketResponse::from).toList());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }

    private void requireAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new org.springframework.security.access.AccessDeniedException("Only support admins can perform this action");
        }
    }
}
