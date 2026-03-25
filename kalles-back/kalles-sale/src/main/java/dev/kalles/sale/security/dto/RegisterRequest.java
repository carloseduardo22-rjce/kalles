package dev.kalles.sale.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Nome é obrigatório")
    String name,

    @NotBlank(message = "Nome da empresa é obrigatório")
    String companyName,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password
) {}