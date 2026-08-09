package dev.kalles.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTicketRequest(

    @Schema(description = "ID of the agent to assign to this ticket")
    @NotNull
    UUID agentId
) {}
