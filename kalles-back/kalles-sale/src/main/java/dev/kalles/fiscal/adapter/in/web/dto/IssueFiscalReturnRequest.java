package dev.kalles.fiscal.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueFiscalReturnRequest(@NotNull UUID saleId) {
}
