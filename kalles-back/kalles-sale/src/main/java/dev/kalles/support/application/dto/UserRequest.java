package dev.kalles.support.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(

    @Schema(description = "Unique email address of the user", example = "joao@empresa.com")
    @NotBlank @Email
    String email,

    @Schema(description = "Full name of the user", example = "João da Silva")
    @NotBlank @Size(max = 255)
    String name
) {}
