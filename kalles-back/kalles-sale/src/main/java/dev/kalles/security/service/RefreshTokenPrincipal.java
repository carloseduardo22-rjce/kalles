package dev.kalles.security.service;

import dev.kalles.security.entity.Account;

import java.util.UUID;

public record RefreshTokenPrincipal(
        Account account,
        UUID posId
) {
}
