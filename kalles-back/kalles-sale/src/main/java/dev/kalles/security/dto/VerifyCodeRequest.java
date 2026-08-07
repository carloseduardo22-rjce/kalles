package dev.kalles.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyCodeRequest(
    @NotBlank(message = "O e-mail é obrigatório.")
    String email,

    @NotBlank(message = "O código é obrigatório.")
    @Size(min = 6, max = 6, message = "O código deve ter 6 dígitos.")
    String code,

    String tenantId
) {
    public VerifyCodeRequest(String email, String code) {
        this(email, code, null);
    }
}
