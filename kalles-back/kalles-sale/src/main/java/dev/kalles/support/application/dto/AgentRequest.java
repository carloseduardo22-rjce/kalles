package dev.kalles.support.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentRequest(

    @Schema(description = "Unique employee ID (badge number or HR code)", example = "EMP-042")
    @NotBlank @Size(max = 100)
    String employeeId,

    @Schema(description = "Full name of the agent", example = "Maria Santos")
    @NotBlank @Size(max = 255)
    String name
) {}
