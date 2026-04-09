package dev.kalles.sale.security.service;

import dev.kalles.sale.security.domain.Account;

import java.util.UUID;

public record RefreshTokenPrincipal(
        Account account,
        UUID posId
) {
}
