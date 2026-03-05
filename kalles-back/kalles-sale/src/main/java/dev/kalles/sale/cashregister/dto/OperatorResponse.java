package dev.kalles.sale.cashregister.dto;

import dev.kalles.sale.cashregister.entity.Operator;

import java.util.UUID;

public record OperatorResponse(
    UUID id,
    String name,
    String code,
    String permissionLevel
) {
    public static OperatorResponse fromEntity(Operator operator) {
        return new OperatorResponse(
            operator.getId(),
            operator.getName(),
            operator.getCode(),
            operator.getPermissionLevel() != null ? operator.getPermissionLevel().name() : null
        );
    }
}
