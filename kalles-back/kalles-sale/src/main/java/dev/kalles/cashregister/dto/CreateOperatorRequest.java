package dev.kalles.cashregister.dto;

import dev.kalles.cashregister.enums.PermissionLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOperatorRequest(

    @Schema(description = "Nome completo do operador", example = "João Silva")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Matrícula do operador, única na filial", example = "joao.silva")
    @NotBlank @Size(max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Matrícula deve conter apenas letras sem acento, números, ponto, underscore ou hífen")
    String code,

    @Schema(description = "E-mail de acesso do operador", example = "joao.silva@loja.com")
    @NotBlank @Email @Size(max = 255)
    String email,

    @Schema(description = "Senha inicial de acesso do operador")
    @NotBlank @Size(min = 8, max = 72)
    String password,

    @Schema(description = "Nível de permissão: BASIC, SUPERVISOR, MANAGER ou ADMIN")
    @NotNull
    PermissionLevel permissionLevel
) {
    public CreateOperatorRequest {
        name = name == null ? null : name.trim();
        code = code == null ? null : code.trim().toLowerCase();
        email = email == null ? null : email.trim().toLowerCase();
    }
}
