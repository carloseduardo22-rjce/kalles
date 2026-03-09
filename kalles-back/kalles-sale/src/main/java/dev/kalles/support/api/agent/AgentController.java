package dev.kalles.support.api.agent;

import dev.kalles.support.application.dto.AgentRequest;
import dev.kalles.support.application.dto.AgentResponse;
import dev.kalles.support.application.service.AgentService;
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
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Tag(name = "Agents", description = "Management of support agents")
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    @Operation(summary = "List active agents", description = "Returns all active agents ordered by name.")
    public ResponseEntity<List<AgentResponse>> listAll() {
        return ResponseEntity.ok(
                agentService.listAllActive().stream().map(AgentResponse::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agent by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agent found"),
        @ApiResponse(responseCode = "404", description = "Agent not found")
    })
    public ResponseEntity<AgentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(AgentResponse.from(agentService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create agent")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Agent created"),
        @ApiResponse(responseCode = "400", description = "Invalid data or duplicate employee ID")
    })
    public ResponseEntity<AgentResponse> create(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AgentResponse.from(agentService.create(request.employeeId(), request.name())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update agent")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agent updated"),
        @ApiResponse(responseCode = "404", description = "Agent not found"),
        @ApiResponse(responseCode = "400", description = "Employee ID already in use")
    })
    public ResponseEntity<AgentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(AgentResponse.from(agentService.update(id, request.employeeId(), request.name())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate agent", description = "Marks the agent as inactive (soft delete).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Agent deactivated"),
        @ApiResponse(responseCode = "404", description = "Agent not found")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        agentService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
