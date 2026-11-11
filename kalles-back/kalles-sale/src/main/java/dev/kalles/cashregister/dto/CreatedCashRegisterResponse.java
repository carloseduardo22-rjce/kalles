package dev.kalles.cashregister.dto;

import dev.kalles.cashregister.entity.CashRegister;

import java.util.UUID;

public record CreatedCashRegisterResponse(UUID id, String code, String description) {

    public static CreatedCashRegisterResponse from(CashRegister cashRegister) {
        return new CreatedCashRegisterResponse(cashRegister.getId(), cashRegister.getCode(), cashRegister.getDescription());
    }
}
