package dev.kalles.sale.cashregister.dto;

import dev.kalles.sale.core.enums.operator.PermissionLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OperatorRequest(

    @Schema(description = "Nome completo do operador", example = "João Silva")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Código de login único do operador", example = "joao.silva")
    @NotBlank @Size(max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "CÃ³digo deve conter apenas letras, nÃºmeros, ponto, underscore ou hÃ­fen")
    String code,

    @Schema(description = "Nível de permissão: BASIC, SUPERVISOR, MANAGER ou ADMIN")
    @NotNull
    PermissionLevel permissionLevel
) {
    public OperatorRequest {
        name = normalize(name);
        code = normalize(code) == null ? null : normalize(code).toLowerCase();
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
