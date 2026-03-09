package dev.kalles.support.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OpenTicketRequest(

    @Schema(description = "Short title describing the issue", example = "Login page returns 500 error")
    @NotBlank @Size(max = 255)
    String title,

    @Schema(description = "Full description of the issue", example = "When I try to login with my admin credentials the server crashes.")
    @NotBlank
    String description,

    @Schema(description = "Email of the user opening the ticket", example = "joao@empresa.com")
    @NotBlank @Email
    String userEmail,

    @Schema(description = "Full name of the user opening the ticket", example = "João da Silva")
    @NotBlank @Size(max = 255)
    String userName,

    @Schema(description = "ID of the category for this ticket")
    @NotNull
    UUID categoryId
) {}
