package dev.kalles.sale.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password,

    String tenantId
) {
    public LoginRequest(String email, String password) {
        this(email, password, null);
    }
}
